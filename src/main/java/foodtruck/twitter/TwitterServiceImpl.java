package foodtruck.twitter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TweetCacheDAO;
import foodtruck.email.EmailNotifier;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckObserver;
import foodtruck.model.TruckStop;
import foodtruck.model.TweetSummary;
import foodtruck.monitoring.Monitored;
import foodtruck.schedule.OffTheRoadDetector;
import foodtruck.schedule.TerminationDetector;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.TruckStopMatcher;
import foodtruck.truckstops.TruckStopNotifier;
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
  private final TweetCacheUpdater remoteUpdater;
  private final TruckDAO truckDAO;
  private final TruckStopNotifier notifier;
  private final ConfigurationDAO configDAO;
  private final EmailNotifier emailNotifier;
  private final OffTheRoadDetector offTheRoadDetector;
  private final GeoLocator locator;

  @Inject
  public TwitterServiceImpl(TwitterFactoryWrapper twitter, TweetCacheDAO tweetDAO,
      DateTimeZone zone,
      TruckStopMatcher matcher, TruckStopDAO truckStopDAO, Clock clock,
      TerminationDetector detector, TweetCacheUpdater updater, TruckDAO truckDAO,
      TruckStopNotifier truckStopNotifier, ConfigurationDAO configDAO,
      EmailNotifier notifier, OffTheRoadDetector offTheRoadDetector, GeoLocator locator) {
    this.tweetDAO = tweetDAO;
    this.twitterFactory = twitter;
    this.defaultZone = zone;
    this.matcher = matcher;
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
    this.terminationDetector = detector;
    this.remoteUpdater = updater;
    this.truckDAO = truckDAO;
    this.notifier = truckStopNotifier;
    this.configDAO = configDAO;
    this.emailNotifier = notifier;
    this.offTheRoadDetector = offTheRoadDetector;
    this.locator = locator;
  }

  @Override @Monitored
  public void updateFromRemoteCache() {
    // This is expensive
    Client client = Client.create();
    WebResource resource = client.resource(configDAO.find().getRemoteTwitterCacheAddress() + "/services/tweets/" +
        tweetDAO.getLastTweetId());
    JSONArray arr = resource.get(JSONArray.class);
    boolean first = true;
    ImmutableList.Builder<TweetSummary> summaries = ImmutableList.builder();
    for (int i = 0; i < arr.length(); i++) {
      try {
        JSONObject tweetObj = arr.getJSONObject(i);
        TweetSummary.Builder builder = new TweetSummary.Builder()
            .id(tweetObj.getLong("id"))
            .text(tweetObj.getString("text"))
            .userId(tweetObj.getString("user"))
            .time(new DateTime(tweetObj.getLong("time"), defaultZone));
        JSONObject locationObj = tweetObj.optJSONObject("location");
        if (locationObj != null) {
          builder.location(Location.builder().lat(tweetObj.getDouble("lat")).lng(tweetObj.getDouble("lng")).build());
        }
        TweetSummary tweetSummary = builder.build();
        if (first) {
          tweetDAO.setLastTweetId(tweetSummary.getId());
          first = false;
        }
        summaries.add(tweetSummary);
      } catch (JSONException e) {
        throw Throwables.propagate(e);
      }
    }
    tweetDAO.save(summaries.build());
  }


  @Override @Monitored
  public void updateTwitterCache() {
    Twitter twitter = twitterFactory.create();
    try {
      Paging paging = determinePaging();
      int twitterListId = Integer.parseInt(configDAO.find().getPrimaryTwitterList());
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
    Collection<Truck> trucks = truckDAO.findByTwitterId(screenName);
    if (trucks.size() == 0 || status.isRetweet()) {
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
    handleTruckTweets();
  }

  public void observerTwittalyze() {
    //TODO: This should all be configurable
    LocalDate today = clock.currentDay();
    DateTime now = clock.now();
    Location uofc = locator.locate("58th and Ellis, Chicago, IL", GeolocationGranularity.NARROW);
    Map<Truck, TweetSummary> trucksAdded = Maps.newHashMap();
    List<TruckStop> truckStops = Lists.newLinkedList();
    for (TruckObserver observer : ImmutableList.of(new TruckObserver("uchinomgo", uofc),
        new TruckObserver("mdw2mnl", uofc))) {
      final List<TweetSummary> tweets = tweetDAO.findTweetsAfter(clock.now().minusHours(HOURS_BACK_TO_SEARCH),
          observer.getTwitterHandle(), false);
      for (TweetSummary tweet : tweets) {
        if (tweet.getIgnoreInTwittalyzer()) {
          continue;
        }
        String lowerText = tweet.getText().toLowerCase();
        if ((lowerText.contains("#foodtrucks") || lowerText.contains("breakfast") || lowerText.contains("lunch"))
            && !tweet.isReply()) {
          for (String twitterHandle : parseHandles(tweet.getText())) {
            Truck truck = Iterables.getFirst(truckDAO.findByTwitterId(twitterHandle), null);
            if (truck == null || trucksAdded.containsKey(truck)) {
              continue;
            }
            List<TruckStop> trucks = truckStopDAO.findDuring(truck.getId(), today);
            if (trucks.isEmpty()) {
              truckStops.add(new TruckStop(truck, now, now.plusHours(2), uofc, null, false));
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
    int pos = 0;
    while (matcher.find()) {
      String twitterId = matcher.group(0).substring(1);
      builder.add(twitterId.toLowerCase());
    }
    return builder.build();
  }


  private void handleTruckTweets() {
    for (Truck truck : truckDAO.findAllTwitterTrucks()) {
      // TODO: this number should probably be configurable
      List<TweetSummary> tweets =
          tweetDAO.findTweetsAfter(clock.now().minusHours(HOURS_BACK_TO_SEARCH),
              truck.getTwitterHandle(),
              false);
      TruckStopMatch match = findMatch(tweets, truck);
      DateTime terminationTime = findTermination(tweets, truck);
      notifyIfOffTheRoad(tweets, truck);
      ignoreTweets(tweets);
      if (match != null) {
        log.log(Level.INFO, "Found match {0}", match);
        List<TruckStop> currentStops = truckStopDAO.findDuring(truck.getId(), clock.currentDay());
        List<TruckStop> deleteStops = Lists.newLinkedList();
        List<TruckStop> addStops = Lists.newLinkedList();
        TruckStop matchedStop = match.getStop();
        for (TruckStop stop : currentStops) {
          boolean locationsSame = stop.getLocation().equals(matchedStop.getLocation());
          // matched start time is contained within stop.  delete stop and make matched stop take
          // new start time
          if (stop.getEndTime().isAfter(matchedStop.getStartTime()) &&
              stop.getStartTime().isBefore(matchedStop.getStartTime())) {
            if (stop.isLocked()) {
              matchedStop = null;
              break;
            }
            deleteStops.add(stop);
            if (locationsSame) {
              if (match.isSoftEnding()) {
                matchedStop = matchedStop.withEndTime(stop.getEndTime());
              } else {
                matchedStop = matchedStop.withStartTime(stop.getStartTime());
              }
            } else {
              addStops.add(stop.withEndTime(matchedStop.getStartTime()));
            }
          } else if (stop.getStartTime().isBefore(matchedStop.getEndTime()) &&
              stop.getEndTime().isAfter(matchedStop.getEndTime())) {
            if (stop.isLocked()) {
              matchedStop = null;
              break;
            }
            if ((locationsSame && !match.isTerminated()) ||
                (stop.getStartTime().getHourOfDay() == 11
                    && stop.getStartTime().getMinuteOfHour() == 30)) {
              deleteStops.add(stop);
              matchedStop = matchedStop.withEndTime(stop.getEndTime());
            } else {
              matchedStop = matchedStop.withEndTime(stop.getStartTime());
            }
          } else if ((stop.getStartTime().equals(matchedStop.getStartTime()) ||
              stop.getStartTime().isAfter(matchedStop.getStartTime())) &&
              (stop.getEndTime().equals(matchedStop.getEndTime()) ||
                  stop.getEndTime().isBefore(matchedStop.getEndTime()))) {
            if (stop.isLocked()) {
              matchedStop = null;
              break;
            }
            deleteStops.add(stop);
            matchedStop = matchedStop.withStartTime(stop.getStartTime());
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
            notifier.added(stop);
          }
          truckStopDAO.addStops(addStops);
        }
      } else if (terminationTime != null) {
        capLastMatchingStop(truck, terminationTime);
      } else {
        log.log(Level.FINE, "No matches for {0}", truck.getId());
      }
    }
  }

  private void capLastMatchingStop(Truck truck, DateTime terminationTime) {
    List<TruckStop> currentStops = truckStopDAO.findDuring(truck.getId(), clock.currentDay());
    TruckStop found = null;
    for (TruckStop stop : currentStops) {
      if (stop.activeDuring(terminationTime)) {
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
  private void ignoreTweets(List<TweetSummary> tweets) {
    List<TweetSummary> l = Lists.newLinkedList();
    for (TweetSummary tweet : tweets) {
      TweetSummary summary = new TweetSummary.Builder(tweet).ignoreInTwittalyzer(true).build();
      l.add(summary);
    }
    tweetDAO.save(l);
  }

  @Override public List<TweetSummary> findByTwitterHandle(String twitterHandle) {
    return tweetDAO
        .findTweetsAfter(clock.currentDay().toDateMidnight().toDateTime(), twitterHandle, true);
  }

  @Override public @Nullable TweetSummary findByTweetId(long id) {
    return tweetDAO.findByTweetId(id);

  }

  @Override public void save(TweetSummary summary) {
    tweetDAO.saveOrUpdate(summary);

  }

  private void notifyIfOffTheRoad(List<TweetSummary> tweets, Truck truck) {
    for (TweetSummary tweet : tweets) {
      if (tweet.getIgnoreInTwittalyzer()) {
        continue;
      }
      if (offTheRoadDetector.offTheRoad(tweet.getText())) {
        try {
          emailNotifier.systemNotifyOffTheRoad(truck, tweet);
          return;
        } catch (Exception e) {
          log.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }

  private @Nullable DateTime findTermination(List<TweetSummary> tweets, Truck truck) {
    for (TweetSummary tweet : tweets) {
      if (tweet.getIgnoreInTwittalyzer()) {
        continue;
      }
      DateTime terminationTime = terminationDetector.detect(tweet);
      if (terminationTime != null) {
        log.log(Level.INFO, "Detected termination for truck: {0} with tweet: {1}",
            new Object[]{truck.getId(), tweet.getText()});
        return terminationTime;
      }
    }
    return null;
  }

  private TruckStopMatch findMatch(List<TweetSummary> tweets, Truck truck) {
    for (TweetSummary tweet : tweets) {
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
}
