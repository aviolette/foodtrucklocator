package foodtruck.notifications;

import java.util.Map;
import java.util.Set;

import foodtruck.linxup.Stop;
import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.TrackingDevice;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.server.vendor.LoginMethod;

/**
 * @author aviolette
 * @since 4/29/13
 */
public interface SystemNotificationService {
  /**
   * Send notification when an off-the-road tweet has occurred
   * @param truck the truck that is the subject of the tweet
   * @param tweet the tweet
   */
  void systemNotifyOffTheRoad(Truck truck, Story tweet);

  /**
   * Send system notification when a new address is added to the system by way of a parsed tweet.
   * @param location the location
   * @param tweet the tweet that triggered the lookup
   * @param truck the truck that owns the tweet
   */
  void systemNotifyLocationAdded(Location location, Story tweet, Truck truck);

  /**
   * Sends system notification when new stops are added by regional observers.
   * @param trucksAdded the map of truckIds
   */
  void systemNotifyTrucksAddedByObserver(Map<Truck, Story> trucksAdded);

  /**
   * Sends out a system notification when a stop is auto-canceled.
   */
  void systemNotifyAutoCanceled(Truck truck, Story tweet);

  /**
   * Sends out an email when an error occurs
   * @param error the error
   */
  void systemNotifyWarnError(String error);

  /**
   * Sends out a notification when a vendor logs in via a portal
   * @param screenName the vendor's screen name
   * @param loginMethod the login method
   */
  void systemNotifyVendorPortalLogin(String screenName, LoginMethod loginMethod);

  /**
   * System notification that is sent out when a truck's location is added and other trucks
   * are mentioned at that spot that don't already have an overlapping stop.
   */
  void notifyAddMentionedTrucks(Set<String> truckIds, TruckStop stop, String text);

  void notifyDeviceAnomalyDetected(Stop stop, TrackingDevice device);
}
