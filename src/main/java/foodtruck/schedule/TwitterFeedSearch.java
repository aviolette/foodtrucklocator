package foodtruck.schedule;

import java.util.List;
import java.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.TruckStop;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * A strategy to pull time and location data out of a food truck's twitter feed.
 * 
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
public class TwitterFeedSearch {
  private static final Logger log = Logger.getLogger(TwitterFeedSearch.class.getName());
  private final TwitterFactoryWrapper twitterFactory;
  private final GeoLocator geolocator;
  private final int twitterListId;
  private AddressExtractor addressExtractor;

  @Inject
  public TwitterFeedSearch(TwitterFactoryWrapper twitter, GeoLocator geolocator,
      @Named("foodtruck.twitter.list") int twitterListId, AddressExtractor extractor) {
    this.twitterFactory = twitter;
    this.geolocator = geolocator;
    this.twitterListId = twitterListId;
    this.addressExtractor = extractor;
  }
  /*
  public List<TruckStopMatch> findForTime(TimeRange range) {
    Twitter twitter = twitterFactory.create();
    ImmutableList.Builder<TruckStop> builder = ImmutableList.builder();
    try {
      List<Status> statuses = twitter.getUserTimeline(truck.getTwitterHandle());
      TruckStopSegment prevSegment = null;
      for (Status status : statuses) {
        TruckStopSegment segment = parseSegment(status);
        if (segment == null) {
          continue;
        }

        if (prevSegment == null) {
          prevSegment = segment;
        } else {
          TruckStop stop = prevSegment.toTruckStop(segment);
          if (stop.within(range)) {
            builder.add(stop);
          }
        }
      }
    } catch (TwitterException e) {
      // TODO: change to something better
      throw new RuntimeException(e);
    }
    return builder.build();
  }
  */
  @VisibleForTesting
  TruckStopSegment parseSegment(Status status) {
    // TODO: implement
    return null;
  }

  public List<TruckStopMatch> findTweets(int hoursBackToSearch) {
    Twitter twitter = twitterFactory.create();
    ImmutableList.Builder<TruckStopMatch> matches = ImmutableList.builder();
    try {
      List<Status> statuses = twitter.getUserListStatuses(twitterListId, new Paging());

      for (Status status : statuses) {
        // TODO: make sure all trucks have their twitter ID as their truckID



      }
    } catch (TwitterException e) {
      throw new RuntimeException(e);
    }
    return matches.build();
  }

  @VisibleForTesting
  static class TruckStopSegment {
    public TruckStop toTruckStop(TruckStopSegment segment) {

      

      return null;
    }
  }

}
