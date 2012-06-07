package foodtruck.truckstops;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import foodtruck.model.Location;
import foodtruck.model.TruckStatus;
import foodtruck.model.DailySchedule;
import foodtruck.model.Truck;
import foodtruck.model.TruckLocationGroup;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStop;

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

  /**
   * Returns schedule for all the trucks on the current day.
   * @param day a day
   * @return the current schedule
   */
  DailySchedule findStopsForDay(LocalDate day);

  void updateStopsForTruck(LocalDate instant, Truck truck);

  /**
   * Finds a food truck stop by id.
   * @param stopId the stop id
   * @return the truck stop or {@code null} if it could not be found
   */
  @Nullable TruckStop findById(long stopId);

  /**
   * Deletes a stop specified by id
   * @param stopId the stop id
   */
  void delete(long stopId);

  /**
   * Updates a truck stop with new information.
   */
  void update(TruckStop truckStop);

  List<TruckStatus> findCurrentAndPreviousStop(LocalDate day);

  /**
   * Syncs the location (based on name) to all the truck stops on the current day.
   */
  void updateLocationInCurrentSchedule(Location location);
}
