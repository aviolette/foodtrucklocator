package foodtruck.twitter;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.util.ByteStreams;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Configuration;
import foodtruck.model.Truck;
import twitter4j.PagableResponseList;
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
  private final ConfigurationDAO configurationDAO;

  @Inject
  public ProfileSyncServiceImpl(TwitterFactoryWrapper twitterFactory, GcsService cloudStorage, TruckDAO truckDAO,
      ConfigurationDAO configDAO) {
    this.twitterFactory = twitterFactory;
    this.cloudStorage = cloudStorage;
    this.truckDAO = truckDAO;
    this.configurationDAO = configDAO;
  }

  @Override
  public Truck createFromTwitter(Truck truck) {
    Twitter twitter = twitterFactory.create();
    try {
      ResponseList<User> lookup = twitter.users().lookupUsers(new String[]{truck.getTwitterHandle()});
      User user = Iterables.getFirst(lookup, null);
      if (user != null) {
        Configuration configuration = configurationDAO.find();
        String url = syncToGoogleStorage(user.getScreenName(), user.getProfileImageURL(),
            configuration.getBaseUrl(), configuration.getTruckIconsBucket());
        String website = user.getURLEntity().getExpandedURL();
        truck = Truck.builder(truck)
            .name(user.getName())
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

  private String syncToGoogleStorage(String twitterHandle, String ogIconUrl, String baseUrl, String bucket) {
    try {
      // If the twitter profile exists, then get the icon URL
      String extension = ogIconUrl.substring(ogIconUrl.lastIndexOf(".")),
          fileName = twitterHandle + extension;
      // copy icon to google cloud storage
      GcsFilename gcsFilename = new GcsFilename(bucket, fileName);
      GcsOutputChannel channel = cloudStorage.createOrReplace(gcsFilename,
          new GcsFileOptions.Builder().mimeType(fileName.matches("png") ? "image/png" : "image/jpeg")
              .build());
      URL iconUrl = new URL(ogIconUrl);
      InputStream in = iconUrl.openStream();
      OutputStream out = Channels.newOutputStream(channel);
      try {
        ByteStreams.copy(in, out);
      } finally {
        in.close();
        out.close();
      }
      ogIconUrl = baseUrl + "/images/truckicons/" + fileName;
    } catch (Exception io) {
      log.log(Level.WARNING, io.getMessage(), io);
    }
    return ogIconUrl;
  }

  @Override
  public void syncFromTwitterList(String primaryTwitterList) {
    Twitter twitter = twitterFactory.create();
    Configuration configuration = configurationDAO.find();
    String baseUrl = configurationDAO.find().getBaseUrl();
    int twitterListId = Integer.parseInt(Strings.nullToEmpty(configuration.getPrimaryTwitterList()));
    long cursor = -1;
    try {
      PagableResponseList<User> result;
      do {
        result = twitter.list().getUserListMembers(twitterListId, cursor);
        for (User user : result) {
          String twitterHandle = user.getScreenName().toLowerCase();
          String url = syncToGoogleStorage(twitterHandle, user.getProfileImageURL(), baseUrl,
              configuration.getTruckIconsBucket());
          truckDAO.save(Truck.builder()
                  .id(twitterHandle)
                  .name(user.getName())
                  .twitterHandle(twitterHandle)
                  .iconUrl(url)
                  .build());
        }
        cursor = result.getNextCursor();
      } while (!result.isEmpty());
    } catch (TwitterException e) {
      throw Throwables.propagate(e);
    }
  }}
