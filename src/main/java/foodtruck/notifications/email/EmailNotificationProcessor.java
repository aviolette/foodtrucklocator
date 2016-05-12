package foodtruck.notifications.email;

import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.email.EmailSender;
import foodtruck.model.NotificationType;
import foodtruck.notifications.NotificationProcessor;
import foodtruck.notifications.PushNotification;

/**
 * @author aviolette
 * @since 2/17/16
 */
public class EmailNotificationProcessor implements NotificationProcessor {
  private static final Logger log = Logger.getLogger(EmailNotificationProcessor.class.getName());

  private final EmailSender sender;

  @Inject
  public EmailNotificationProcessor(EmailSender sender) {
    this.sender = sender;
  }

  @Override
  public void handle(PushNotification notification) {
    if (notification.getType() != NotificationType.EMAIL) {
      return;
    }
    log.info("Sending message: " + notification.getMessage());
    sender.sendMessage(notification.getSummary(),
        ImmutableList.of(notification.getDeviceToken()),
        notification.getMessage(), ImmutableList.<String>of(), null);
  }
}
