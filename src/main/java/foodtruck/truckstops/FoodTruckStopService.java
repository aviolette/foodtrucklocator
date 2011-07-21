package foodtruck.truckstops;

import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import foodtruck.model.TruckStop;

/**
 * Service for manipulating food truck stops.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public interface FoodTruckStopService {
  /**
   * Finds all the truck stops for the current time.
   * @param instant the time to search on.
   * @return the truck stops (sorted by the name of the truck in ascending order 
   */
  Set<TruckStop> findStopsFor(DateTime instant);

  /**
   * Updates the location data for the current day.
   * @param day running the process for this day
   */
  void updateStopsFor(LocalDate day);
}
