package foodtruck.notifications;

import java.util.logging.Logger;

import com.google.inject.AbstractModule;

import foodtruck.notifications.twitter.TwitterEventNotificationService;

/**
 * @author aviolette
 * @since 12/3/12
 */
public class NotificationModule extends AbstractModule {
  private static final Logger log = Logger.getLogger(NotificationModule.class.getName());

  @Override protected void configure() {
    bind(EventNotificationService.class).to(TwitterEventNotificationService.class);
  }
}
