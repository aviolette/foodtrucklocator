package foodtruck.email;

import java.util.Map;

import foodtruck.model.FoodTruckRequest;
import foodtruck.model.Location;
import foodtruck.model.PetitionSignature;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.model.TweetSummary;
import foodtruck.server.vendor.LoginMethod;

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
   * Send a notifications to all food trucks of a food truck request.
   */
  boolean notifyFoodTrucksOfRequest(Iterable<String> addresses, FoodTruckRequest request);

  /**
   * Sends out a system notification when a stop is auto-canceled.
   */
  void systemNotifyAutoCanceled(Truck truck, TweetSummary tweet);

  /**
   * Sends out a system notification when a stop is added after 1:30 for a lunch truck via the twittalyzer
   */

  void systemNotifiyWeirdStopAdded(TruckStop truckStop, String tweetText);

  /**
   * Sends out an email when an error occurs
   * @param error the error
   */
  void systemNotifyWarnError(String error);

  void notifyVerifyPetitionSignature(PetitionSignature sig);

  void notifyThanksForSigningPetition(PetitionSignature sig);

  /**
   * Sends out a notification when a vendor logs in via a portal
   * @param screenName the vendor's screen name
   * @param loginMethod the login method
   */
  void systemNotifyVendorPortalLogin(String screenName, LoginMethod loginMethod);
}
