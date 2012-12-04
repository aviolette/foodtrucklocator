package foodtruck.notifications;

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
}
