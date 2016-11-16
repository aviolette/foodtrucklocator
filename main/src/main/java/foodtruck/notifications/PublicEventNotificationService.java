package foodtruck.notifications;

import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.TruckStop;

/**
 * Sends out high-level events to any channel that listens.
 * @author aviolette
 * @since 12/3/12
 */
public interface PublicEventNotificationService {
  /**
   * Broadcasts location-based notifications to people who subscribe.  Currently, this puts notifications on
   * location-specific twitter accounts.
   */
  void sendLunchtimeNotifications();

  /**
   * Updates the embedded location stored in the notifications with new information.
   * @param location the location
   */
  void updateLocationInNotifications(Location location);

  /**
   * Sends out a notification that a truck has started at a particular location.  Selects appropriate channels to send
   * it out of.
   *
   * @param truckStop the truck stop
   */
  void notifyStopStart(TruckStop truckStop);

  /**
   * Sends out a notification that a truck has ended at a particular location.
   *
   * @param truckStop the truck stop
   */
  void notifyStopEnd(TruckStop truckStop);

  /**
   * Shares the story (if possible) associated with the matched truck stop.
   */
  void share(Story story, TruckStop stop);
}
