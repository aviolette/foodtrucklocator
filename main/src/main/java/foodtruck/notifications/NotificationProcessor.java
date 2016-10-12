package foodtruck.notifications;

/**
 * @author aviolette
 * @since 2/17/16
 */
public interface NotificationProcessor {
  void handle(PushNotification notification);
}
