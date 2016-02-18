package foodtruck.notifications;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.email.EmailSender;
import foodtruck.model.NotificationType;

/**
 * @author aviolette
 * @since 2/17/16
 */
public class EmailNotificationProcessor implements NotificationProcessor {
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
    sender.sendMessage("New food trucks open", ImmutableList.of(notification.getDeviceToken()),
        notification.getMessage(), ImmutableList.<String>of(), null);
  }
}
