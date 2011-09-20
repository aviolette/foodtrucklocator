package foodtruck.schedule;

import java.util.List;
import java.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * A strategy to pull time and location data out of a food truck's twitter feed.
 * 
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
public class TwitterFeedScheduleStrategy implements ScheduleStrategy {
  private static final Logger log = Logger.getLogger(TwitterFeedScheduleStrategy.class.getName());
  private final TwitterFactoryWrapper twitterFactory;
  private final GeoLocator geolocator;

  public TwitterFeedScheduleStrategy(TwitterFactoryWrapper twitter, GeoLocator geolocator) {
    this.twitterFactory = twitter;
    this.geolocator = geolocator;
  }
  
  @Override
  public List<TruckStop> findForTime(Truck truck, TimeRange range) {
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

  @VisibleForTesting TruckStopSegment parseSegment(Status status) {
    // TODO: implement
    return null;
  }

  @VisibleForTesting static class TruckStopSegment {
    public TruckStop toTruckStop(TruckStopSegment segment) {

      

      return null;
    }
  }

}
