package foodtruck.socialmedia;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import foodtruck.dao.TweetCacheDAO;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.Story;
import foodtruck.model.StoryType;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author aviolette
 * @since 8/26/15
 */
public class TwitterConnector implements SocialMediaConnector {
  private static final Logger log = Logger.getLogger(TwitterConnector.class.getName());

  private final TwitterFactoryWrapper twitterFactory;
  private final DateTimeZone defaultZone;
  private final StaticConfig config;
  private final TweetCacheDAO tweetDAO;

  @Inject
  public TwitterConnector(TwitterFactoryWrapper twitter, DateTimeZone defaultZone, StaticConfig config,
      TweetCacheDAO tweetDAO) {
    this.twitterFactory = twitter;
    this.defaultZone = defaultZone;
    this.config = config;
    this.tweetDAO = tweetDAO;
  }

  @Override
  public List<Story> recentStories() {
    ImmutableList.Builder<Story> stories = ImmutableList.builder();
    Twitter twitter = twitterFactory.create();
    try {
      Paging paging = determinePaging();
      int twitterListId = Integer.parseInt(config.getPrimaryTwitterList());
      List<Status> statuses = twitter.getUserListStatuses(twitterListId, paging);
      boolean first = true;
      for (Status status : statuses) {
        if (first) {
          // tweets come in descending order...so this is the tweet we want to start from next time
          tweetDAO.setLastTweetId(status.getId());
          first = false;
        }
        Story tweet = statusToTweet(status);
        if (tweet != null) {
          log.log(Level.INFO, "Tweet {0}: ", tweet);
          stories.add(tweet);
        }
      }
    } catch (TwitterException e) {
      throw new RuntimeException(e);
    }
    return stories.build();
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

  private @Nullable Story statusToTweet(Status status) {
    final String screenName = status.getUser().getScreenName().toLowerCase();
    if (status.isRetweet()) {
      return null;
    }
    final GeoLocation geoLocation = status.getGeoLocation();
    Location location = null;
    if (geoLocation != null) {
      location = Location.builder().lat(geoLocation.getLatitude()).lng(geoLocation.getLongitude()).build();
    }
    final DateTime tweetTime = new DateTime(status.getCreatedAt(), defaultZone);
    return new Story.Builder().userId(screenName)
        .location(location)
        .id(status.getId())
        .time(tweetTime)
        .type(StoryType.TWEET)
        .text(status.getText())
        .build();
  }
}
