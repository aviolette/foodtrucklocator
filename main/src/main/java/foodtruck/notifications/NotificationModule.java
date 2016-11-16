package foodtruck.notifications;

import com.google.inject.AbstractModule;

/**
 * @author aviolette
 * @since 12/3/12
 */
public class NotificationModule extends AbstractModule {
  @Override protected void configure() {
    bind(PublicEventNotificationService.class).to(TwitterEventNotificationService.class);
    bind(SystemNotificationService.class).to(SimpleEmailNotifier.class);
    bind(EmailSender.class).to(JavaMailEmailSender.class);
  }
}
