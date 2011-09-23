package foodtruck.schedule;

import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.Trucks;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * A strategy to pull time and location data out of a food truck's twitter feed.
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
public class TwitterFeedSearch {
  private static final Logger log = Logger.getLogger(TwitterFeedSearch.class.getName());
  private final TwitterFactoryWrapper twitterFactory;
  private final int twitterListId;
  private final TruckStopMatcher truckStopMatcher;
  private final Trucks trucks;
  private final DateTimeZone defaultZone;

  @Inject
  public TwitterFeedSearch(TwitterFactoryWrapper twitter,  TruckStopMatcher truckStopMatcher,
      @Named("foodtruck.twitter.list") int twitterListId, Trucks trucks, DateTimeZone zone) {
    this.twitterFactory = twitter;
    this.twitterListId = twitterListId;
    this.truckStopMatcher = truckStopMatcher;
    this.trucks = trucks;
    this.defaultZone = zone;
  }

  public Multimap<String, TruckStopMatch> findTweets(int hoursBackToSearch) {
    Twitter twitter = twitterFactory.create();
    Multimap<String, TruckStopMatch> matches = HashMultimap.create();
    try {
      List<Status> statuses = twitter.getUserListStatuses(twitterListId, new Paging());

      for (Status status : statuses) {
        Truck truck = trucks.findByTwitterId(status.getUser().getScreenName().toLowerCase());
        if (truck == null) {
          log.warning("Could not find truck specified by screen name: " + status.getUser().getScreenName());
          continue;
        }

        Location location = null;
        final GeoLocation geoLocation = status.getGeoLocation();
        if (geoLocation != null) {
          location = new Location(geoLocation.getLatitude(),
              geoLocation.getLongitude());
        }

        final DateTime tweetTime = new DateTime(status.getCreatedAt(), defaultZone);
        TruckStopMatch match = truckStopMatcher.match(status.getText(), truck, location, tweetTime);
        if (match != null) {
          matches.put(truck.getId(), match);
        }

      }
    } catch (TwitterException e) {
      throw new RuntimeException(e);
    }
    return matches;
  }

}
