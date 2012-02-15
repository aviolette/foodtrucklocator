package foodtruck.twitter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
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
import foodtruck.schedule.TerminationDetector;
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
  @VisibleForTesting static final int HOURS_BACK_TO_SEARCH = 6;
  private final TweetCacheDAO tweetDAO;
  private final TwitterFactoryWrapper twitterFactory;
  private final int twitterListId;
  private final Trucks trucks;
  private final DateTimeZone defaultZone;
  private final TruckStopMatcher matcher;
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;
  private final TerminationDetector terminationDetector;
  private final TweetCacheUpdater remoteUpdater;

  @Inject
  public TwitterServiceImpl(TwitterFactoryWrapper twitter, TweetCacheDAO tweetDAO,
      @Named("foodtruck.twitter.list") int twitterListId, Trucks trucks, DateTimeZone zone,
      TruckStopMatcher matcher, TruckStopDAO truckStopDAO, Clock clock,
      TerminationDetector detector, TweetCacheUpdater updater) {
    this.tweetDAO = tweetDAO;
    this.twitterFactory = twitter;
    this.twitterListId = twitterListId;
    this.trucks = trucks;
    this.defaultZone = zone;
    this.matcher = matcher;
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
    this.terminationDetector = detector;
    this.remoteUpdater = updater;
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
          log.log(Level.INFO, "Tweet {0}: ", tweet);
          summaries.add(tweet);
        }
      }
      tweetDAO.save(summaries);
      remoteUpdater.update(summaries);
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
      location = Location.builder().lat(geoLocation.getLatitude())
          .lng(geoLocation.getLongitude()).build();
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

  /**
   * For trucks that use the twittalyzer, this code takes the existing schedule in the database
   * (derived from calendar data and past tweets) and merges it with new twitter matches.
   * The algorithm does the following:
   * If there is a recent tweet that matches to a truck stop:
   * for each existing stop:
   * 1) If the existing stop's end time overlaps with the matched stop's start time,
   * shorten the existing stop's end time to be the same as the matched stop's start time.
   * 2) If the existing stop's start time overlaps with the matched stop's end time, shorten
   * the matched stop's start time.
   * 3) If the existing stop's contained with the match, then delete the stop
   * 4) If the match is contained within the stop, then delete the stop
   */
  @Override
  public void twittalyze() {
    log.log(Level.INFO, "Updating twitter trucks");
    boolean changed = false;
    for (Truck truck : trucks.allTwitterTrucks()) {
      // TODO: this number should probably be configurable
      List<TweetSummary> tweets =
          tweetDAO.findTweetsAfter(clock.now().minusHours(HOURS_BACK_TO_SEARCH), truck.getId());
      TruckStopMatch match = findMatch(tweets, truck);
      if (match != null) {
        log.log(Level.INFO, "Found match {0}", match);
        List<TruckStop> currentStops = truckStopDAO.findDuring(truck.getId(), clock.currentDay());
        List<TruckStop> deleteStops = Lists.newLinkedList();
        List<TruckStop> addStops = Lists.newLinkedList();
        TruckStop matchedStop = match.getStop();
        for (TruckStop stop : currentStops) {
          boolean locationsSame = stop.getLocation().equals(matchedStop.getLocation());
          if (stop.getEndTime().isAfter(matchedStop.getStartTime()) &&
              stop.getStartTime().isBefore(matchedStop.getStartTime())) {
            deleteStops.add(stop);
            if (locationsSame) {
              matchedStop = matchedStop.withStartTime(stop.getStartTime());
            } else {
              addStops.add(stop.withEndTime(matchedStop.getStartTime()));
            }
          } else if (stop.getStartTime().isBefore(matchedStop.getEndTime()) &&
              stop.getEndTime().isAfter(matchedStop.getEndTime())) {
            if (locationsSame && !match.isTerminated()) {
              deleteStops.add(stop);
              matchedStop = matchedStop.withEndTime(stop.getEndTime());
            } else {
              matchedStop = matchedStop.withEndTime(stop.getStartTime());
            }
          } else if ((stop.getStartTime().equals(matchedStop.getStartTime()) ||
              stop.getStartTime().isAfter(matchedStop.getStartTime())) &&
              (stop.getEndTime().equals(matchedStop.getEndTime()) ||
                  stop.getEndTime().isBefore(matchedStop.getEndTime()))) {
            deleteStops.add(stop);
            matchedStop = matchedStop.withStartTime(stop.getStartTime());
          }
        }
        if (!deleteStops.isEmpty()) {
          truckStopDAO.deleteStops(deleteStops);
        }
        changed = true;
        addStops.add(matchedStop);
        truckStopDAO.addStops(addStops);
      } else {
        log.log(Level.INFO, "No matches for {0}", truck.getId());
      }
    }
  }

  @Override public List<TweetSummary> findForTruck(String truckId) {
    return tweetDAO.findTweetsAfter(clock.currentDay().toDateMidnight().toDateTime(), truckId);
  }

  @Override public @Nullable TweetSummary findByTweetId(long id) {
    return tweetDAO.findByTweetId(id);

  }

  @Override public void save(TweetSummary summary) {
    tweetDAO.saveOrUpdate(summary);

  }

  private TruckStopMatch findMatch(List<TweetSummary> tweets, Truck truck) {
    DateTime terminationTime = null;
    for (TweetSummary tweet : tweets) {
      if (tweet.getIgnoreInTwittalyzer()) {
        log.log(Level.INFO, "Ignoring tweet: {0}", tweet);
        continue;
      }
      if (terminationTime == null) {
        log.log(Level.INFO, "Found tweet {0}", tweet);
        terminationTime = terminationDetector.detect(tweet);
        if (terminationTime != null) {
          log.log(Level.INFO, "Detected termination");
          continue;
        }
      }
      TruckStopMatch match = matcher.match(truck, tweet, terminationTime);
      if (match != null) {
        return match;
      }
    }
    return null;
  }
}
