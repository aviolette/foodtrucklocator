package foodtruck.socialmedia;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import foodtruck.dao.StoryDAO;
import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.StoryType;
import foodtruck.model.Truck;
import foodtruck.model.TwitterNotificationAccount;
import foodtruck.monitoring.Monitored;
import foodtruck.util.ServiceException;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.conf.PropertyConfiguration;

/**
 * @author aviolette
 * @since 8/26/15
 */
public class TwitterConnector implements SocialMediaConnector {
  private static final int TWITTER_LIST_ID = 56683487;
  private static final Logger log = Logger.getLogger(TwitterConnector.class.getName());

  private final Provider<TwitterFactoryWrapper> twitterFactoryWrapperProvider;
  private final DateTimeZone defaultZone;
  private final StoryDAO tweetDAO;
  private final Properties properties;

  @Inject
  public TwitterConnector(Provider<TwitterFactoryWrapper> twitterProvider, DateTimeZone defaultZone, StoryDAO tweetDAO,
      @foodtruck.annotations.Twitter Properties properties) {
    this.twitterFactoryWrapperProvider = twitterProvider;
    this.defaultZone = defaultZone;
    this.tweetDAO = tweetDAO;
    this.properties = properties;
  }

  @Override @Monitored
  public List<Story> recentStories() {
    ImmutableList.Builder<Story> stories = ImmutableList.builder();
    Twitter twitter = twitterFactoryWrapperProvider.get().create();
    try {
      Paging paging = determinePaging();
      List<Status> statuses;
      statuses = twitter.getUserListStatuses(TWITTER_LIST_ID, paging);
      boolean first = true;
      for (Status status : statuses) {
        if (first) {
          // tweets come in descending order...so this is the tweet we want to start from next time
          tweetDAO.setLastTweetId(status.getId());
          first = false;
        }
        statusToTweet(status).ifPresent(tweet -> {
          log.log(Level.INFO, "Tweet {0}: ", tweet);
          stories.add(tweet);
        });
      }
    } catch (TwitterException e) {
      throw new RuntimeException(e);
    }
    return stories.build();
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void updateStatusFor(ScheduleMessage message, Truck truck) throws ServiceException {
    if (!truck.getHasTwitterCredentials()) {
      return;
    }
    Twitter twitter = twitterFactoryWrapperProvider.get().createDetached(
        new AccessToken(truck.getTwitterToken(), truck.getTwitterTokenSecret()));
    try {
      for (String messageComponent : message.getTwitterMessages()) {
        twitter.updateStatus(messageComponent);
      }
    } catch (TwitterException e) {
      throw new ServiceException(e);
    }
  }

  @Override @Monitored
  public void sendStatusFor(String message, Truck truck, MessageSplitter splitter) {
    twitterAccessToken(truck).ifPresent(
        token -> splitAndSend(message, splitter, twitterFactoryWrapperProvider.get().createDetached(token)));
  }

  @Override @Monitored
  public void sendStatusFor(String message, TwitterNotificationAccount account,
      MessageSplitter splitter) {
    log.log(Level.INFO, "Initial status: {0}", new Object[]{message});
    Twitter twitter = twitterFactoryWrapperProvider.get().createDetached(twitterCredentials(account));
    splitAndSend(message, splitter, twitter);
  }

  private void splitAndSend(String message, MessageSplitter splitter, Twitter twitter)
      throws ServiceException {
    try {
      for (String theStatus : splitter.split(message)) {
        log.log(Level.INFO, "Sending status update for account {0}: {1}",
            new Object[]{twitter.getAccountSettings().getScreenName(), theStatus});
        twitter.updateStatus(theStatus);
      }
    } catch (TwitterException e) {
      throw new ServiceException(e);
    }
  }

  private Paging determinePaging() {
    long sinceId = tweetDAO.getLastTweetId();
    Paging paging;
    if (sinceId == 0) {
      paging = new Paging();
    } else {
      paging = new Paging(sinceId);
    }
    return paging;
  }

  private Optional<Story> statusToTweet(Status status) {
    final String screenName = status.getUser()
        .getScreenName()
        .toLowerCase();
    if (status.isRetweet()) {
      return Optional.empty();
    }
    if (status.isTruncated()) {
      log.log(Level.WARNING, "Status truncated: {0}", status);
    }
    final GeoLocation geoLocation = status.getGeoLocation();
    Location location = null;
    if (geoLocation != null) {
      location = Location.builder()
          .lat(geoLocation.getLatitude())
          .lng(geoLocation.getLongitude())
          .build();
    }
    final DateTime tweetTime = new DateTime(status.getCreatedAt(), defaultZone);
    return Optional.of(new Story.Builder().userId(screenName)
        .location(location)
        .id(status.getId())
        .time(tweetTime)
        .type(StoryType.TWEET)
        .text(status.getText())
        .build());
  }

  @Monitored
  public void retweet(long storyId, TwitterNotificationAccount account) {
    Twitter twitter = twitterFactoryWrapperProvider.get().createDetached(twitterCredentials(account));
    try {
      twitter.retweetStatus(storyId);
    } catch (TwitterException e) {
      throw new ServiceException(e);
    }
  }

  private Optional<AccessToken> twitterAccessToken(Truck truck) {
    if (truck.getHasTwitterCredentials()) {
      //noinspection ConstantConditions
      return Optional.of(new AccessToken(truck.getTwitterToken(), truck.getTwitterTokenSecret()));
    }
    return Optional.empty();
  }

  private PropertyConfiguration twitterCredentials(TwitterNotificationAccount account) {
    Properties truckProps = new Properties(properties);
    truckProps.put("oauth.accessToken", account.getOauthToken());
    truckProps.put("oauth.accessTokenSecret", account.getOauthTokenSecret());
    return new PropertyConfiguration(truckProps);
  }
}
