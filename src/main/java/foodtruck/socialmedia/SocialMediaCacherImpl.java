package foodtruck.socialmedia;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.RetweetsDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckObserverDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TweetCacheDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.email.EmailNotifier;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.StopOrigin;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckObserver;
import foodtruck.model.TruckStop;
import foodtruck.model.TwitterNotificationAccount;
import foodtruck.monitoring.Monitored;
import foodtruck.schedule.Confidence;
import foodtruck.schedule.OffTheRoadDetector;
import foodtruck.schedule.OffTheRoadResponse;
import foodtruck.schedule.TerminationDetector;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.TruckStopMatcher;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.truckstops.TruckStopNotifier;
import foodtruck.util.Clock;
import foodtruck.util.TimeOnlyFormatter;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public class SocialMediaCacherImpl implements SocialMediaCacher {
  private static final Logger log = Logger.getLogger(SocialMediaCacherImpl.class.getName());
  private static final Pattern TWITTER_PATTERN = Pattern.compile("@([\\w|\\d|_]+)");

  @VisibleForTesting
  static final int HOURS_BACK_TO_SEARCH = 6;
  private final TweetCacheDAO tweetDAO;
  private final TwitterFactoryWrapper twitterFactory;
  private final DateTimeZone defaultZone;
  private final TruckStopMatcher matcher;
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;
  private final TerminationDetector terminationDetector;
  private final TruckDAO truckDAO;
  private final TruckStopNotifier notifier;
  private final EmailNotifier emailNotifier;
  private final OffTheRoadDetector offTheRoadDetector;
  private final GeoLocator locator;
  private final TruckObserverDAO truckObserverDAO;
  private final TwitterNotificationAccountDAO notificationAccountDAO;
  private final RetweetsDAO retweetsDAO;
  private final FoodTruckStopService truckStopService;
  private final DateTimeFormatter timeFormatter;
  private final StaticConfig staticConfig;

  @Inject
  public SocialMediaCacherImpl(TwitterFactoryWrapper twitter, TweetCacheDAO tweetDAO, DateTimeZone zone,
      TruckStopMatcher matcher, TruckStopDAO truckStopDAO, Clock clock, TerminationDetector detector, TruckDAO truckDAO,
      TruckStopNotifier truckStopNotifier, EmailNotifier notifier, OffTheRoadDetector offTheRoadDetector,
      GeoLocator locator, TruckObserverDAO truckObserverDAO, TwitterNotificationAccountDAO notificationAccountDAO,
      RetweetsDAO retweetsDAO, FoodTruckStopService truckStopService,
      @TimeOnlyFormatter DateTimeFormatter timeFormatter, StaticConfig staticConfig) {
    this.tweetDAO = tweetDAO;
    this.twitterFactory = twitter;
    this.defaultZone = zone;
    this.matcher = matcher;
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
    this.terminationDetector = detector;
    this.truckDAO = truckDAO;
    this.notifier = truckStopNotifier;
    this.emailNotifier = notifier;
    this.offTheRoadDetector = offTheRoadDetector;
    this.locator = locator;
    this.truckObserverDAO = truckObserverDAO;
    this.notificationAccountDAO = notificationAccountDAO;
    this.retweetsDAO = retweetsDAO;
    this.truckStopService = truckStopService;
    this.timeFormatter = timeFormatter;
    this.staticConfig = staticConfig;
  }

  @Override @Monitored
  public void update() {
    Twitter twitter = twitterFactory.create();
    try {
      Paging paging = determinePaging();
      int twitterListId = Integer.parseInt(staticConfig.getPrimaryTwitterList());
      List<Status> statuses = twitter.getUserListStatuses(twitterListId, paging);
      boolean first = true;
      List<Story> summaries = Lists.newLinkedList();
      for (Status status : statuses) {
        if (first) {
          // tweets come in descending order...so this is the tweet we want to start from next time
          tweetDAO.setLastTweetId(status.getId());
          first = false;
        }
        Story tweet = statusToTweet(status);
        if (tweet != null) {
          log.log(Level.INFO, "Tweet {0}: ", tweet);
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

  private @Nullable Story statusToTweet(Status status) {
    final String screenName = status.getUser().getScreenName().toLowerCase();
    if (status.isRetweet()) {
      return null;
    }
    final GeoLocation geoLocation = status.getGeoLocation();
    Location location = null;
    if (geoLocation != null) {
      location = Location.builder().lat(geoLocation.getLatitude())
          .lng(geoLocation.getLongitude()).build();
    }
    final DateTime tweetTime = new DateTime(status.getCreatedAt(), defaultZone);
    return new Story.Builder()
        .userId(screenName)
        .location(location)
        .id(status.getId())
        .time(tweetTime)
        .text(status.getText())
        .build();
  }

  @Override
  public void purgeBefore(LocalDate localDate) {
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
  public void analyze() {
    log.log(Level.INFO, "Updating twitter trucks");
    handleTruckTweets();
    observerAnalyze();
  }

  public void observerAnalyze() {
    LocalDate today = clock.currentDay();
    DateTime now = clock.now();
    Location uofc = locator.locate("58th and Ellis, Chicago, IL", GeolocationGranularity.NARROW);
    Map<Truck, Story> trucksAdded = Maps.newHashMap();
    List<TruckStop> truckStops = Lists.newLinkedList();
    for (TruckObserver observer : truckObserverDAO.findAll()) {
      final List<Story> tweets = tweetDAO.findTweetsAfter(clock.now().minusHours(HOURS_BACK_TO_SEARCH),
          observer.getTwitterHandle(), false);
      for (Story tweet : tweets) {
        if (tweet.getIgnoreInTwittalyzer()) {
          continue;
        }
        log.log(Level.INFO, "Handling observer tweet: {0}", tweet);
        String lowerText = tweet.getText().toLowerCase();
        if (observer.containsKeyword(lowerText) && !tweet.isReply()) {
          for (String twitterHandle : parseHandles(tweet.getText())) {
            Truck truck = Iterables.getFirst(truckDAO.findByTwitterId(twitterHandle), null);
            if (truck == null || trucksAdded.containsKey(truck)) {
              continue;
            }
            List<TruckStop> trucks = truckStopDAO.findDuring(truck.getId(), today);
            if (trucks.isEmpty()) {
              DateTime startTime = now;
              if (now.getHourOfDay() < 11 && !truck.getCategories().contains("Breakfast")) {
                startTime = now.withTime(11, 0, 0, 0);
              }
              truckStops.add(
                  TruckStop.builder().origin(StopOrigin.OBSERVER)
                      .truck(truck).startTime(startTime).endTime(startTime.plusHours(2))
                      .location(uofc)
                      .appendNote("Added by @" + observer.getTwitterHandle() + " at " +
                          clock.nowFormattedAsTime()
                          + " from tweet '" + tweet.getText() + "'")
                      .build());
              trucksAdded.put(truck, tweet);
            }
          }
        }
      }
      if (!tweets.isEmpty()) {
        ignoreTweets(tweets);
      }
    }
    if (!truckStops.isEmpty()) {
      truckStopDAO.addStops(truckStops);
      emailNotifier.systemNotifyTrucksAddedByObserver(trucksAdded);
    }
  }

  private Set<String> parseHandles(String text) {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    Matcher matcher = TWITTER_PATTERN.matcher(text);
    while (matcher.find()) {
      String twitterId = matcher.group(0).substring(1);
      builder.add(twitterId.toLowerCase());
    }
    return builder.build();
  }

  @VisibleForTesting
  void handleTruckTweets() {
    for (Truck truck : truckDAO.findAll()) {
      if (Strings.isNullOrEmpty(truck.getTwitterHandle())) {
        continue;
      }
      List<Story> tweets =
          tweetDAO.findTweetsAfter(clock.now().minusHours(HOURS_BACK_TO_SEARCH),
              truck.getTwitterHandle(),
              false);
      notifyIfOffTheRoad(tweets, truck);
      if (!truck.isUsingTwittalyzer()) {
        log.log(Level.FINE, "Twittalyzer isn't enabled for {0}", truck.getId());
        continue;
      }
      TruckStopMatch match = findMatch(tweets, truck);
      DateTime terminationTime = findTermination(tweets, truck);
      ignoreTweets(tweets);
      if (match != null) {
        handleStopMatch(truck, match);
      } else if (terminationTime != null) {
        capLastMatchingStop(truck, terminationTime);
      } else {
        log.log(Level.FINE, "No matches for {0}", truck.getId());
      }
    }
  }

  private void handleStopMatch(Truck truck, TruckStopMatch match) {
    log.log(Level.INFO, "Found match {0}", match);
    List<TruckStop> currentStops = truckStopDAO.findDuring(truck.getId(), clock.currentDay()),
                    deleteStops = Lists.newLinkedList(),
                    addStops = Lists.newLinkedList();
    TruckStop matchedStop = match.getStop();
    for (TruckStop stop : currentStops) {
      boolean locationsSame = stop.getLocation().equals(matchedStop.getLocation());
      // matched start time is contained within stop.  delete stop and make matched stop take
      // new start time
      final DateTime stopEnd = stop.getEndTime(), matchedStart = matchedStop.getStartTime(),
          stopStart = stop.getStartTime();
      // current stop: 7am - 9am
      // matched stop: 8am - 930am
      // if location the same, and soft ending => 7am-9am; if
      TruckStop.Builder matchBuilder = TruckStop.builder(matchedStop).lastUpdated(clock.now());
      if (stopEnd.isAfter(matchedStart) && stopStart.isBefore(matchedStart)) {
        if (stop.isLocked()) {
          matchedStop = null;
          break;
        }
        deleteStops.add(stop);
        if (locationsSame) {
          matchBuilder.prependNotes(stop.getNotes()).origin(stop.getOrigin());
          if (match.isSoftEnding()) {
            if (!stopEnd.equals(matchedStop.getEndTime())) {
              matchBuilder.appendNote(String.format("Changed end time to %s from %s ", timeFormatter.print(stopEnd),
                  timeFormatter.print(matchedStop.getEndTime())))
                .endTime(stopEnd);
            }
            if (clock.now().isAfter(stopStart) && !stopStart.equals(matchedStart)) {
              matchBuilder.appendNote("Setting start time to " + timeFormatter.print(stopStart))
                  .startTime(stopStart);
            }
            matchedStop = matchBuilder.build();
          } else {
            if (!stopStart.equals(matchedStart)) {
              matchBuilder.appendNote("Setting start time to: " + timeFormatter.print(stopStart));
            }
            matchedStop = matchBuilder.startTime(stopStart).build();
          }
        } else {
          addStops.add(TruckStop.builder(stop).endTime(matchedStart).appendNote(String.format("Truncated stop based on tweet: '%s'", match.getText())).build());
        }
      } else {
        final DateTime matchedEnd = matchedStop.getEndTime();
        if (stopStart.isBefore(matchedEnd) && stopEnd.isAfter(matchedEnd)) {
          if (stop.isLocked()) {
            matchedStop = null;
            break;
          }
          deleteStops.add(stop);
          if ((locationsSame && !match.isTerminated()) ||
              (stopStart.getHourOfDay() == 11
                  && stopStart.getMinuteOfHour() == 30)) {
            if (!matchedEnd.equals(stopEnd)) {
              matchBuilder.appendNote("Changed end time to " + timeFormatter.print(stopEnd));
            }
            matchedStop = matchBuilder.prependNotes(stop.getNotes()).endTime(stopEnd).build();
          } else {
            if (!matchedStart.equals(stopStart)) {
              matchBuilder.appendNote("Changing end time to " + timeFormatter.print(stopStart));
            }
            matchedStop = matchBuilder.endTime(stopStart).build();
          }
        } else if ((stopStart.equals(matchedStart) ||
            stopStart.isAfter(matchedStart)) &&
            (stopEnd.equals(matchedEnd) ||
                stopEnd.isBefore(matchedEnd))) {
          if (stop.isLocked()) {
            matchedStop = null;
            break;
          }
          deleteStops.add(stop);
          if (!matchedStart.equals(stopStart)) {
            matchBuilder.appendNote("Changed start time to " + timeFormatter.print(stopStart));
          }
          matchedStop = matchBuilder.startTime(stopStart).build();
        }
      }
    }
    if (!deleteStops.isEmpty()) {
      for (TruckStop stop : deleteStops) {
        notifier.removed(stop);
      }
      truckStopDAO.deleteStops(deleteStops);
    }
    if (matchedStop != null) {
      addStops.add(matchedStop);
      for (TruckStop stop : addStops) {
        checkForRetweet(stop, match);
        checkIsWeird(stop, match);
        notifier.added(stop);
      }
      truckStopDAO.addStops(addStops);
    }
    compressAdjacentStops(truck.getId(), clock.currentDay());
  }

  private void checkIsWeird(TruckStop stop, TruckStopMatch match) {
    if (stop.getOrigin() == StopOrigin.TWITTER &&
        match.getConfidence() == Confidence.LOW &&
        stop.getTruck().getCategories().contains("Lunch") &&
        stop.getStartTime().isAfter(clock.timeAt(13, 30))) {
      log.info("STOP TIME: " + stop.getStartTime() + ", FOO: " + clock.timeAt(13,30));
      emailNotifier.systemNotifiyWeirdStopAdded(stop, match.getText());
    }
  }

  private void checkForRetweet(TruckStop stop, TruckStopMatch match) {
    try {
      log.log(Level.INFO, "Checking for retweets against {0} {1}", new Object[] {stop, match});
      for (TwitterNotificationAccount account : notificationAccountDAO.findAll()) {
        if (retweetsDAO.hasBeenRetweeted(stop.getTruck().getId(), account.getTwitterHandle())) {
          log.log(Level.INFO, "Already retweeted at {0} {1}", new Object[] {stop.getTruck().getId(), account.getTwitterHandle()});
          continue;
        }
        if (stop.getLocation().containedWithRadiusOf(account.getLocation())) {
          Twitter twitter = new TwitterFactory(account.twitterCredentials()).getInstance();
          try {
            log.log(Level.INFO, "RETWEETING:" + match.getText());
            retweetsDAO.markRetweeted(stop.getTruck().getId(), account.getTwitterHandle());
            twitter.retweetStatus(match.getTweetId());
          } catch (TwitterException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
          }
        } else {
          log.log(Level.INFO, "{0} not contained within radius of {1}",
              new Object[] {stop.getLocation(), account.getLocation()});
        }
      }
    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
  }

  private void capLastMatchingStop(Truck truck, DateTime terminationTime) {
    List<TruckStop> currentStops = truckStopDAO.findDuring(truck.getId(), clock.currentDay());
    TruckStop found = null;
    for (TruckStop stop : currentStops) {
      if (stop.activeDuring(terminationTime)) {
        if (stop.getStartTime().plusMinutes(10).isAfter(terminationTime)) {
          // This logic is to test the case where a cupcake truck or some truck with a lot of stops might say
          // 'thanks' at one spot like 3 minutes into their next scheduled spot, capping the next scheduled spot
          // when they were really saying thans for the previous spot.
          log.log(Level.INFO, "Didn't cap spot since it was w/in a threshold of ten minutes for stop {0} " +
              "and termination time {1}", new Object[] {stop, terminationTime});
          return;
        }
        found = stop;
        break;
      }
    }
    if (found == null) {
      log.log(Level.INFO, "No Matching stop found to terminate");
      return;
    }
    log.log(Level.INFO, "Capping {0} with new termination time {1}", new Object[]{found,
        terminationTime});
    found = found.withEndTime(terminationTime);
    notifier.terminated(found);
    log.log(Level.INFO, "New stop {0}", found);
    truckStopDAO.save(found);
  }

  /**
   * Ignore tweets so they are not matched in upcoming requests
   */
  private void ignoreTweets(List<Story> tweets) {
    if (tweets.isEmpty()) {
      return;
    }
    List<Story> l = Lists.newLinkedList();
    for (Story tweet : tweets) {
      Story summary = new Story.Builder(tweet).ignoreInTwittalyzer(true).build();
      l.add(summary);
    }
    tweetDAO.save(l);
  }

  private void notifyIfOffTheRoad(List<Story> tweets, Truck truck) {
    for (Story tweet : tweets) {
      if (tweet.getIgnoreInTwittalyzer()) {
        continue;
      }
      final OffTheRoadResponse offTheRoadResponse = offTheRoadDetector.offTheRoad(tweet.getText());
      if (offTheRoadResponse.isOffTheRoad()) {
        if (offTheRoadResponse.isConfidenceHigh() && staticConfig.isAutoOffRoad()) {
          log.log(Level.INFO, "Auto canceling stops for truck {0} based on tweet: {1}",
              new Object[] { truck.getId(), tweet.getText()} );
          int count = truckStopService.cancelRemainingStops(truck.getId(), clock.now());
          if (count > 0) {
            emailNotifier.systemNotifyAutoCanceled(truck, tweet);
          } else {
            log.log(Level.INFO, "No stops to actually cancel");
          }
          if (!truck.isUsingTwittalyzer()) {
            ignoreTweets(ImmutableList.of(tweet));
          }
          return;
        } else {
          try {
            emailNotifier.systemNotifyOffTheRoad(truck, tweet);
            if (!truck.isUsingTwittalyzer()) {
              log.log(Level.FINE, "Ignoring tweet {0} cause truck is not using twittalyzer", tweet.getText());
              ignoreTweets(ImmutableList.of(tweet));
            }
            return;
          } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
    }
  }

  private @Nullable DateTime findTermination(List<Story> tweets, Truck truck) {
    for (Story tweet : tweets) {
      if (tweet.getIgnoreInTwittalyzer()) {
        continue;
      }
      if (truck.match(tweet.getText())) {
        DateTime terminationTime = terminationDetector.detect(tweet);
        if (terminationTime != null) {
          log.log(Level.INFO, "Detected termination for truck: {0} with tweet: {1}",
              new Object[]{truck.getId(), tweet.getText()});
          return terminationTime;
        }
      }
    }
    return null;
  }

  private TruckStopMatch findMatch(List<Story> tweets, Truck truck) {
    for (Story tweet : tweets) {
      if (tweet.getIgnoreInTwittalyzer()) {
        log.log(Level.INFO, "Ignoring tweet: {0}", tweet);
        continue;
      }
      DateTime terminationTime = terminationDetector.detect(tweet);
      if (terminationTime != null) {
        return null;
      }
      TruckStopMatch match = matcher.match(truck, tweet, null);
      if (match != null) {
        return match;
      }
    }
    return null;
  }

  private void compressAdjacentStops(String truckId, LocalDate day) {
    TruckStop previousStop =  null;
    for (TruckStop stop : truckStopDAO.findDuring(truckId, day)) {
      if (previousStop != null) {
        DateTime pEnd = previousStop.getEndTime(), cStart = stop.getStartTime();
        // If two stops are adjacent, then combine them
        if (pEnd.getHourOfDay() == cStart.getHourOfDay() && pEnd.getMinuteOfDay() == cStart.getMinuteOfDay()
            && stop.getLocation().containedWithRadiusOf(previousStop.getLocation())) {
          truckStopDAO.delete((Long) previousStop.getKey());
          stop = TruckStop.builder(stop)
              .startTime(previousStop.getStartTime())
              .build();
          truckStopDAO.save(stop);
        // If two stops are the exact same time and location, then delete the previousStop
        } else if (previousStop.getStartTime().equals(stop.getStartTime())
            && previousStop.getEndTime().equals(stop.getEndTime())
            && stop.getLocation().containedWithRadiusOf(previousStop.getLocation())) {
          truckStopDAO.delete((Long) previousStop.getKey());
        }
      }
      previousStop = stop;
    }
  }

}
