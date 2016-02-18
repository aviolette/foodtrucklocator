package foodtruck.notifications;

import java.util.logging.Level;
import java.util.logging.Logger;

import foodtruck.model.NotificationType;

/**
 * @author aviolette
 * @since 2/17/16
 */
public class PushNotificationProcessor implements NotificationProcessor {
  private static final Logger log = Logger.getLogger(PushNotificationProcessor.class.getName());

  @Override
  public void handle(PushNotification notification) {
    if (notification.getType() != NotificationType.PUSH) {
      return;
    }
    log.log(Level.FINE, "Push notification" + notification.getType());
  }
}
