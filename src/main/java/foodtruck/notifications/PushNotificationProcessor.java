package foodtruck.notifications;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

import foodtruck.model.NotificationType;

/**
 * @author aviolette
 * @since 2/17/16
 */
public class PushNotificationProcessor implements NotificationProcessor {
  private static final Logger log = Logger.getLogger(PushNotificationProcessor.class.getName());
  private final ApnsService apnsService;

  @Inject
  public PushNotificationProcessor(ApnsService service) {
    this.apnsService = service;
  }

  @Override
  public void handle(PushNotification notification) {
    if (notification.getType() != NotificationType.PUSH) {
      return;
    }
    String payload = APNS.newPayload().alertBody(notification.getMessage()).build();
    log.log(Level.INFO, "Push notification for device {0}: {1}", new Object[]{notification.getDeviceToken(), payload});
    apnsService.push(notification.getDeviceToken(), payload);
  }
}
