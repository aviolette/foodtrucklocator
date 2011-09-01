package foodtruck.truckstops;

import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import foodtruck.model.TruckLocationGroup;

/**
 * Service for manipulating food truck stops.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public interface FoodTruckStopService {
  /**
   * Updates the location data for the current day.
   * @param day running the process for this day
   */
  void updateStopsFor(LocalDate day);

  /**
   * Returns the food trucks, grouped by locations.
   */
  Set<TruckLocationGroup> findFoodTruckGroups(DateTime dateTime);
}
