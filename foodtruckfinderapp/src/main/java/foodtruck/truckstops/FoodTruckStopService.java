package foodtruck.truckstops;

import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import foodtruck.model.TruckLocationGroup;
import foodtruck.model.TruckSchedule;

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

  /**
   * Returns a list of truck stops for a truck on a particular day
   * @param truckId the truck id
   * @param day the day
   * @return a list of truck stops ordered by time (ascending)
   */
  TruckSchedule findStopsForDay(String truckId, LocalDate day);
}
