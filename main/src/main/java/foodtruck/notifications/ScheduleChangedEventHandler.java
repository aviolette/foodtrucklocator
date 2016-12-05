package foodtruck.notifications;

import com.google.inject.Inject;

import foodtruck.model.Story;
import foodtruck.model.TruckStop;
import foodtruck.schedule.StoryEventCallback;

/**
 * @author aviolette
 * @since 12/5/16
 */
public class ScheduleChangedEventHandler implements StoryEventCallback {
  private final PublicEventNotificationService notificationService;

  @Inject
  public ScheduleChangedEventHandler(PublicEventNotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override
  public void stopAdded(Story story, TruckStop stop) {
    notificationService.share(story, stop);
  }
}
