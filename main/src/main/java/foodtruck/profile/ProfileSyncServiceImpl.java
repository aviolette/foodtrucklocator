package foodtruck.profile;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.annotations.DefaultCityState;
import foodtruck.annotations.IconBucketName;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.socialmedia.FacebookEndpoint;
import foodtruck.socialmedia.TwitterFactoryWrapper;
import foodtruck.storage.StorageService;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * @author aviolette
 * @since 12/30/14
 */
public class ProfileSyncServiceImpl implements ProfileSyncService {

  private static final Logger log = Logger.getLogger(ProfileSyncServiceImpl.class.getName());
  private final Provider<TwitterFactoryWrapper> twitterFactoryProvider;
  private final StorageService storageService;
  private final TruckDAO truckDAO;
  private final Pattern pageUrlPattern = Pattern.compile("/pages/(.*)/(\\d+)");
  private final WebResource facebookResource;
  private final String cityState;
  private final String iconBucket;

  @Inject
  public ProfileSyncServiceImpl(Provider<TwitterFactoryWrapper> twitterFactoryProvider, TruckDAO truckDAO,
      @FacebookEndpoint WebResource facebookResource, StorageService storageService,
      @DefaultCityState String cityState, @IconBucketName String iconBucket) {
    this.twitterFactoryProvider = twitterFactoryProvider;
    this.truckDAO = truckDAO;
    this.facebookResource = facebookResource;
    this.storageService = storageService;
    this.cityState = cityState;
    this.iconBucket = iconBucket;
  }

  @Override
  public Truck createFromTwitter(Truck truck) {
    Twitter twitter = twitterFactoryProvider.get()
        .create();
    try {
      ResponseList<User> lookup = twitter.users()
          .lookupUsers(truck.getTwitterHandle());
      User user = Iterables.getFirst(lookup, null);
      if (user != null) {
        String url = syncToGoogleStorage(user.getScreenName(), user.getProfileImageURL(), iconBucket);
        String website = user.getURLEntity()
            .getExpandedURL();
        truck = Truck.builder(truck)
            .name(user.getName())
            .defaultCity(cityState)
            .categories(ImmutableSet.of("Lunch"))
            .useTwittalyzer(true)
            .url(website)
            .iconUrl(url)
            .build();
      }
    } catch (TwitterException e) {
      log.log(Level.WARNING, "Error contacting twitter", e.getMessage());
    }
    truckDAO.save(truck);
    syncProfile(truck);
    return truck;
  }

  private String syncToGoogleStorage(String twitterHandle, String ogIconUrl, String bucket) {
    try {
      // If the twitter profile exists, then get the icon URL
      String extension = ogIconUrl.substring(ogIconUrl.lastIndexOf(".")), fileName = twitterHandle + extension;
      return storageService.copyUrl(ogIconUrl, bucket, fileName);
    } catch (Exception io) {
      log.log(Level.WARNING, io.getMessage(), io);
    }
    return ogIconUrl;
  }

  @Override
  public void syncProfile(String truckId) {
    truckDAO.findByIdOpt(truckId)
        .ifPresent(this::syncProfile);
  }

  private void syncProfile(Truck truck) {
    log.log(Level.INFO, "Syncing truck {0}", truck.getId());
    boolean changed = false;
    if (!Strings.isNullOrEmpty(truck.getTwitterHandle())) {
      truck = syncFromTwitter(truck);
      changed = true;
    }
    if (!Strings.isNullOrEmpty(truck.getFacebook())) {
      try {
        truck = syncFromFacebookGraph(truck);
        changed = true;
      } catch (Exception e) {
        log.log(Level.WARNING, e.getMessage());
      }
    }
    if (changed) {
      truckDAO.save(truck);
    }
  }

  @Override
  public void syncAllProfiles() {
    for (Truck truck : truckDAO.findAll()) {
      try {
        syncProfile(truck);
      } catch (Exception e) {
        log.warning(e.getMessage());
      }
    }
  }

  private Truck syncFromFacebookGraph(Truck truck) {
    String uri = truck.getFacebook();
    if (uri == null) {
      return truck;
    }
    Matcher m = pageUrlPattern.matcher(uri);
    if (m.find()) {
      uri = "/" + m.group(2);
    }
    log.log(Level.FINE, "Syncing truck: {0}", truck.getId());
    String response = facebookResource.uri(URI.create(uri))
        .get(String.class);
    JSONObject responseObj;
    try {
      responseObj = new JSONObject(response);
    } catch (JSONException e) {
      return truck;
    }
    Truck.Builder builder;
    try {
      builder = Truck.builder(truck)
          .facebookPageId(responseObj.getString("id"));
    } catch (JSONException e) {
      return truck;
    }
    try {
      if (Strings.isNullOrEmpty(truck.getUrl())) {
        builder.url(responseObj.getString("website"));
      }
      if (Strings.isNullOrEmpty(truck.getPhone())) {
        builder.phone(responseObj.getString("phone"));
      }
    } catch (JSONException ignored) {
    }
    // http://graph.facebook.com/overrice.foodtruck/picture?height=400&width=400
    if (Strings.isNullOrEmpty(truck.getPreviewIcon())) {
      try {
        URL iconUrl = new URL("http", "graph.facebook.com", 80, uri + "/picture?width=180&height=180");
        log.log(Level.INFO, "Syncing from URL {0}", iconUrl.toString());
        builder.previewIcon(copyUrlToStorage(iconUrl, iconBucket, truck.getId() + "_preview"));
      } catch (MalformedURLException e) {
        return truck;
      }
    }
    return builder.build();
  }

  private @Nullable
  String copyUrlToStorage(URL iconUrl, String truckIconsBucket, String baseName) {
    try {
      // If the twitter profile exists, then get the icon URL
      // copy icon to google cloud storage
      URLConnection connection = iconUrl.openConnection();
      String mimeType = connection.getContentType();
      if (mimeType.contains("text")) {
        log.log(Level.INFO, "Invalid image type for: " + iconUrl);
        return null;
      }
      String extension = mimeType.contains("jpeg") ? "jpg" : "png", fileName = baseName + "." + extension;
      return storageService.copyUrl(iconUrl.toExternalForm(), truckIconsBucket, fileName);
    } catch (Exception io) {
      log.log(Level.WARNING, io.getMessage(), io);
      throw new RuntimeException(io);
    }
  }

  private Truck syncFromTwitter(Truck truck) {
    Twitter twitter = twitterFactoryProvider.get()
        .create();
    try {
      ResponseList<User> response = twitter.users()
          .lookupUsers(truck.getTwitterHandle());
      User user = Iterables.getFirst(response, null);
      if (user != null) {
        String twitterHandle = user.getScreenName()
            .toLowerCase();
        Truck.Builder builder = Truck.builder(truck);
        if (Strings.isNullOrEmpty(truck.getIconUrl()) || truck.getIconUrl()
            .contains("pbs.")) {
          String url = syncToGoogleStorage(twitterHandle, user.getProfileImageURL(), iconBucket);
          builder.iconUrl(url);
        }
        if (Strings.isNullOrEmpty(truck.getPreviewIcon())) {
          URL iconUrl = new URL(user.getProfileImageURL()
              .replaceAll("_normal", "_400x400"));
          builder.previewIcon(copyUrlToStorage(iconUrl, iconBucket, truck.getId() + "_preview"));
        }
        if (Strings.isNullOrEmpty(truck.getBackgroundImage()) &&
            !Strings.isNullOrEmpty(user.getProfileBannerMobileURL())) {
          URL backgroundUrl = new URL(user.getProfileBannerMobileURL());
          builder.backgroundImage(
              copyUrlToStorage(backgroundUrl, iconBucket, truck.getId() + "_banner"));
        }
        if (Strings.isNullOrEmpty(truck.getBackgroundImageLarge()) &&
            !Strings.isNullOrEmpty(user.getProfileBannerIPadRetinaURL())) {
          URL backgroundUrl = new URL(user.getProfileBannerIPadRetinaURL());
          builder.backgroundImageLarge(
              copyUrlToStorage(backgroundUrl, iconBucket, truck.getId() + "_bannerlarge"));
        }
        return builder.build();
      }
    } catch (TwitterException | MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return truck;
  }
}
