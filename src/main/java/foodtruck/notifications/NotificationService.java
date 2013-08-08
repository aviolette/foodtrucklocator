package foodtruck.notifications;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 12/3/12
 */
public interface NotificationService {
  /**
   * Broadcasts location-based notifications to people who subscribe.  Currently, this puts notifications on
   * location-specific twitter accounts.
   */
  public void sendNotifications();

  /**
   * Updates the embedded location stored in the notifications with new information.
   * @param location the location
   */
  void updateLocationInNotifications(Location location);
}
