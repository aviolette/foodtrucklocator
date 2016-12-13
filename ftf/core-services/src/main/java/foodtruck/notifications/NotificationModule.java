package foodtruck.notifications;

import com.google.inject.AbstractModule;

import foodtruck.schedule.StoryEventCallback;

/**
 * @author aviolette
 * @since 12/3/12
 */
public class NotificationModule extends AbstractModule {
  @Override protected void configure() {
    bind(PublicEventNotificationService.class).to(TwitterEventNotificationService.class);
    bind(StoryEventCallback.class).to(ScheduleChangedEventHandler.class);
  }
}
