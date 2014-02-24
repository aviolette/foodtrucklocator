package foodtruck.email;

import java.util.Map;

import foodtruck.model.FoodTruckRequest;
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
  void systemNotifyOffTheRoad(Truck truck, TweetSummary tweet);

  /**
   * Send system notification when a new address is added to the system by way of a parsed tweet.
   * @param location the location
   * @param tweet the tweet that triggered the lookup
   * @param truck the truck that owns the tweet
   */
  void systemNotifyLocationAdded(Location location, TweetSummary tweet, Truck truck);

  /**
   * Sends system notification when new stops are added by regional observers.
   * @param trucksAdded the map of truckIds
   */
  void systemNotifyTrucksAddedByObserver(Map<Truck, TweetSummary> trucksAdded);

  /**
   * Send a notification when a request has come through.
   */
  void notifyNewFoodTruckRequest(FoodTruckRequest request);

  /**
   * Send a notifications to all food trucks of a food truck request.
   */
  boolean notifyFoodTrucksOfRequest(Iterable<String> addresses, FoodTruckRequest request);
}
