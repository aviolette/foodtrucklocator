package foodtruck.schedule;

import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;

import foodtruck.schedule.ScheduleStrategy;
import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.TwitterFactoryWrapper;
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

  public TwitterFeedScheduleStrategy(TwitterFactoryWrapper twitter) {
    this.twitterFactory = twitter;
  }
  
  @Override
  public List<TruckStop> findForTime(Truck truck, TimeRange range) {
    Twitter twitter = twitterFactory.create();
    try {
      List<Status> statuses = twitter.getUserTimeline(truck.getTwitterHandle());


      
    } catch (TwitterException e) {
      // TODO: change to something better
      throw new RuntimeException(e);
    }
    return ImmutableList.of();
  }
}
