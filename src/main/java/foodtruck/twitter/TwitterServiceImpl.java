package foodtruck.twitter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TweetCacheDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.model.Trucks;
import foodtruck.model.TweetSummary;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.TruckStopMatcher;
import foodtruck.util.Clock;
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
  private static final Logger log = Logger.getLogger(TwitterServiceImpl.class.getName());
  private final TweetCacheDAO tweetDAO;
  private final TwitterFactoryWrapper twitterFactory;
  private final int twitterListId;
  private final Trucks trucks;
  private final DateTimeZone defaultZone;
  private final TruckStopMatcher matcher;
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;

  @Inject
  public TwitterServiceImpl(TwitterFactoryWrapper twitter, TweetCacheDAO tweetDAO,
      @Named("foodtruck.twitter.list") int twitterListId, Trucks trucks, DateTimeZone zone,
      TruckStopMatcher matcher, TruckStopDAO truckStopDAO, Clock clock) {
    this.tweetDAO = tweetDAO;
    this.twitterFactory = twitter;
    this.twitterListId = twitterListId;
    this.trucks = trucks;
    this.defaultZone = zone;
    this.matcher = matcher;
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
  }

  @Override
  public void updateTwitterCache() {
    Twitter twitter = twitterFactory.create();
    try {
      Paging paging = determinePaging();
      List<Status> statuses = twitter.getUserListStatuses(twitterListId, paging);
      boolean first = true;
      List<TweetSummary> summaries = Lists.newLinkedList();
      for (Status status : statuses) {
        if (first) {
          // tweets come in descending order...so this is the tweet we want to start from next time
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

  private @Nullable TweetSummary statusToTweet(Status status) {
    final String screenName = status.getUser().getScreenName().toLowerCase();
    Truck truck = trucks.findByTwitterId(screenName);
    if (truck == null || status.isRetweet()) {
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

  @Override
  public void updateLocationsOfTwitterTrucks() {
    log.log(Level.INFO, "Updating twitter trucks");
    for (Truck truck : trucks.allTwitterTrucks()) {
      List<TweetSummary> tweets =
          tweetDAO.findTweetsAfter(clock.now().minusHours(4), truck.getId());
      TruckStopMatch match = findMatch(tweets, truck);
      if (match != null) {
        log.log(Level.INFO, "Found match {0}", match);
        resolveConflicting(truck, match.getStop());
      } else {
        log.log(Level.INFO, "No matches for {0}", truck.getId());
      }
    }
  }

  private void resolveConflicting(Truck truck, TruckStop stop) {
    // TODO: delete the conflicting stops, not all the stops
    // TODO: find stop-departure/sold-out markers and terminate existing entries.
    List<TruckStop> existingStops = truckStopDAO.findDuring(truck.getId(), clock.currentDay());
    List<TruckStop> toDelete = Lists.newLinkedList();
    List<TruckStop> toAdd = Lists.newLinkedList();
    for (TruckStop existingStop : existingStops) {
      if (existingStop.getEndTime().isBefore(stop.getStartTime())) {
      } else if (existingStop.getStartTime().isBefore(stop.getStartTime())
          && existingStop.getEndTime().isAfter(stop.getStartTime())) {
        // TODO: figure out how to do updates in appengine
        toAdd.add(existingStop.withEndTime(stop.getStartTime()));
        toDelete.add(existingStop);
      } else if(existingStop.getStartTime().isBefore(stop.getEndTime())) {
        // TODO: figure out how to do updates in appengine
        toAdd.add(existingStop.withStartTime(stop.getEndTime()));
        toDelete.add(existingStop);
      }
    }
    toAdd.add(stop);
    truckStopDAO.deleteStops(toDelete);
    truckStopDAO.addStops(toAdd);
  }

  private TruckStopMatch findMatch(List<TweetSummary> tweets, Truck truck) {
    for (TweetSummary tweet : tweets ) {
      TruckStopMatch match = matcher.match(truck, tweet);
      if (match != null) {
        return match;
      }
    }
    return null;
  }
}
