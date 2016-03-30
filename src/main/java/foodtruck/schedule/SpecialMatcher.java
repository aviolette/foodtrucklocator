package foodtruck.schedule;

import foodtruck.model.Story;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 3/29/16
 */
public interface SpecialMatcher {
  void handle(TruckStopMatch.Builder builder, Story story, Truck truck);
}
