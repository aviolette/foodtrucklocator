package foodtruck.email;

import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TweetSummary;

/**
 * @author aviolette
 * @since 4/29/13
 */
public interface EmailNotifier {
  /**
   * Send notification when an off-the-road tweet has occurred
   * @param truck the truck that is the subject of the tweet
   * @param tweet the tweet
   */
  public void systemNotifyOffTheRoad(Truck truck, TweetSummary tweet);

  /**
   * Send system notificaiton when a new address is added to the system by way of a parsed tweet.
   * @param location the location
   * @param tweet the tweet that triggered the lookup
   * @param truck the truck that owns the tweet
   */
  public void systemNotifyLocationAdded(Location location, TweetSummary tweet, Truck truck);
}
