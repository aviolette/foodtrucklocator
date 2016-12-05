package foodtruck.schedule;

import foodtruck.model.Story;
import foodtruck.model.TruckStop;

/**
 * @author aviolette
 * @since 12/5/16
 */
public interface StoryEventCallback {
  void stopAdded(Story story, TruckStop stop);
}
