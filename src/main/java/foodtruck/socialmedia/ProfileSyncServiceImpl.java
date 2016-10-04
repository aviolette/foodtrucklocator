package foodtruck.socialmedia;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.api.client.util.ByteStreams;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.StaticConfig;
import foodtruck.model.Truck;
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
  private final TwitterFactoryWrapper twitterFactory;
  private final GcsService cloudStorage;
  private final TruckDAO truckDAO;
  private final StaticConfig staticConfig;
  private final Pattern pageUrlPattern = Pattern.compile("/pages/(.*)/(\\d+)");
  private final WebResource facebookResource;


  @Inject
  public ProfileSyncServiceImpl(TwitterFactoryWrapper twitterFactory, GcsService cloudStorage, TruckDAO truckDAO,
      StaticConfig staticConfig,  @FacebookEndpoint WebResource facebookResource) {
    this.twitterFactory = twitterFactory;
    this.cloudStorage = cloudStorage;
    this.truckDAO = truckDAO;
    this.staticConfig = staticConfig;
    this.facebookResource = facebookResource;
  }

  @Override
  public Truck createFromTwitter(Truck truck) {
    Twitter twitter = twitterFactory.create();
    try {
      ResponseList<User> lookup = twitter.users().lookupUsers(new String[]{truck.getTwitterHandle()});
      User user = Iterables.getFirst(lookup, null);
      if (user != null) {
        String url = syncToGoogleStorage(user.getScreenName(), user.getProfileImageURL(), staticConfig.getIconBucket());
        String website = user.getURLEntity().getExpandedURL();
        truck = Truck.builder(truck)
            .name(user.getName())
            .defaultCity(staticConfig.getCityState())
            .categories(ImmutableSet.of("Lunch"))
            .backgroundImage(user.getProfileBackgroundImageURL())
            .url(website)
            .iconUrl(url)
            .build();
      }
    } catch (TwitterException e) {
      log.log(Level.WARNING, "Error contacting twitter", e.getMessage());
    }
    truckDAO.save(truck);
    return truck;
  }

  private String syncToGoogleStorage(String twitterHandle, String ogIconUrl, String bucket) {
    try {
      // If the twitter profile exists, then get the icon URL
      String extension = ogIconUrl.substring(ogIconUrl.lastIndexOf(".")),
          fileName = twitterHandle + extension;
      // copy icon to google cloud storage
      GcsFilename gcsFilename = new GcsFilename(bucket, fileName);
      GcsOutputChannel channel = cloudStorage.createOrReplace(gcsFilename,
          new GcsFileOptions.Builder().cacheControl("public, max-age=2591000")
              .mimeType(fileName.matches("png") ? "image/png" : "image/jpeg")
              .build());
      URL iconUrl = new URL(ogIconUrl);
      try (InputStream in = iconUrl.openStream(); OutputStream out = Channels.newOutputStream(channel)) {
        ByteStreams.copy(in, out);
      }
      ogIconUrl = "http://storage.googleapis.com/" + bucket + "/" + fileName;
    } catch (Exception io) {
      log.log(Level.WARNING, io.getMessage(), io);
    }
    return ogIconUrl;
  }

  @Override
  public void syncProfile(String truckId) {
    Truck truck = truckDAO.findById(truckId);
    syncProfile(truck);
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
        builder.previewIcon(
            copyUrlToStorage(iconUrl, staticConfig.getIconBucket(),
                truck.getId() + "_preview"));
      } catch (MalformedURLException e) {
        return truck;
      }
    }
    return builder.build();
  }

  private @Nullable String copyUrlToStorage(URL iconUrl, String truckIconsBucket, String baseName) {
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
      GcsFilename gcsFilename = new GcsFilename(truckIconsBucket, fileName);
      GcsOutputChannel channel = cloudStorage.createOrReplace(gcsFilename,
          new GcsFileOptions.Builder().mimeType(mimeType)
              .cacheControl("public, max-age=2591000")
              .build());
      try (InputStream in = connection.getInputStream(); OutputStream out = Channels.newOutputStream(channel)) {
        ByteStreams.copy(in, out);
      }
      log.log(Level.INFO, "Created file {0} in bucket {1}", new Object[] {fileName, truckIconsBucket});
      return "http://storage.googleapis.com/" + truckIconsBucket + "/" + fileName;
    } catch (Exception io) {
      log.log(Level.WARNING, io.getMessage(), io);
      throw Throwables.propagate(io);
    }
  }

  private Truck syncFromTwitter(Truck truck) {
    Twitter twitter = twitterFactory.create();
    try {
      ResponseList<User> response = twitter.users()
          .lookupUsers(new String[]{truck.getTwitterHandle()});
      User user = Iterables.getFirst(response, null);
      if (user != null) {
        String twitterHandle = user.getScreenName().toLowerCase();
        Truck.Builder builder = Truck.builder(truck);
        if (Strings.isNullOrEmpty(truck.getIconUrl()) || truck.getIconUrl().contains("pbs.")) {
          String url = syncToGoogleStorage(twitterHandle, user.getProfileImageURL(), staticConfig.getIconBucket());
          builder.iconUrl(url);
        }
        if (Strings.isNullOrEmpty(truck.getPreviewIcon())) {
          URL iconUrl = new URL(user.getProfileImageURL().replaceAll("_normal", "_400x400"));
          builder.previewIcon(copyUrlToStorage(iconUrl, staticConfig.getIconBucket(), truck.getId() + "_preview"));
        }
        if (Strings.isNullOrEmpty(truck.getBackgroundImage()) && !Strings.isNullOrEmpty(user.getProfileBannerMobileURL())) {
          URL backgroundUrl = new URL(user.getProfileBannerMobileURL());
          builder.backgroundImage(copyUrlToStorage(backgroundUrl, staticConfig.getIconBucket(), truck.getId() + "_banner"));
        }
        if (Strings.isNullOrEmpty(truck.getBackgroundImageLarge()) && !Strings.isNullOrEmpty(user.getProfileBannerIPadRetinaURL())) {
          URL backgroundUrl = new URL(user.getProfileBannerIPadRetinaURL());
          builder.backgroundImageLarge(copyUrlToStorage(backgroundUrl, staticConfig.getIconBucket(), truck.getId() + "_bannerlarge"));
        }
        return builder.build();
      }
    } catch (TwitterException|MalformedURLException e) {
      throw Throwables.propagate(e);
    }
    return truck;
  }
}
