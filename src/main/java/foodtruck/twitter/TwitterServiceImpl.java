package foodtruck.twitter;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.dao.TweetCacheDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.Trucks;
import foodtruck.model.TweetSummary;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public class TwitterServiceImpl implements TwitterService {
  private final TweetCacheDAO tweetDAO;
  private final TwitterFactoryWrapper twitterFactory;
  private final int twitterListId;
  private final Trucks trucks;
  private final DateTimeZone defaultZone;

  @Inject
  public TwitterServiceImpl(TwitterFactoryWrapper twitter, TweetCacheDAO tweetDAO,
      @Named("foodtruck.twitter.list") int twitterListId, Trucks trucks, DateTimeZone zone) {
    this.tweetDAO = tweetDAO;
    this.twitterFactory = twitter;
    this.twitterListId = twitterListId;
    this.trucks = trucks;
    this.defaultZone = zone;
  }

  @Override
  public void updateTwitterFeedsFor(DateTime localDate) {
    Twitter twitter = twitterFactory.create();
    try {
      long sinceId = tweetDAO.getLastTweetId();
      Paging paging;
      if (sinceId == 0) {
        paging = new Paging();
      } else {
        paging = new Paging(sinceId);
      }
      List<Status> statuses = twitter.getUserListStatuses(twitterListId, paging);
      boolean first = true;
      List<TweetSummary> summaries = Lists.newLinkedList();
      for (Status status : statuses) {
        if (first) {
          tweetDAO.setLastTweetId(status.getId());
          first = false;
        }
        TweetSummary tweet = statusToTweet(status);
        if (tweet != null) {
          summaries.add(tweet);
        }
      }
      tweetDAO.save(summaries);
    } catch (TwitterException e) {
      throw new RuntimeException(e);
    }
  }

  private @Nullable TweetSummary statusToTweet(Status status) {
    final String screenName = status.getUser().getScreenName().toLowerCase();
    Truck truck = trucks.findByTwitterId(screenName);
    if (truck == null) {
      return null;
    }
    final GeoLocation geoLocation = status.getGeoLocation();
    Location location = null;
    if (geoLocation != null) {
      location = new Location(geoLocation.getLatitude(),
          geoLocation.getLongitude());
    }
    final DateTime tweetTime = new DateTime(status.getCreatedAt(), defaultZone);
    return new TweetSummary.Builder()
        .userId(screenName)
        .location(location)
        .id(status.getId())
        .time(tweetTime)
        .text(status.getText())
        .build();
  }

  @Override
  public void purgeTweetsBefore(LocalDate localDate) {
    tweetDAO.deleteBefore(localDate.toDateTimeAtStartOfDay());
  }
}
