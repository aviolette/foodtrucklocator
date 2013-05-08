package foodtruck.truckstops;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckLocationGroup;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStatus;
import foodtruck.model.TruckStop;
import foodtruck.model.WeeklySchedule;

/**
 * Service for manipulating food truck stops.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public interface FoodTruckStopService {
  /**
   * Updates the location data for the current day.
   * @param range the time range to update
   */
  void updateStopsFor(Interval range);

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

  void updateStopsForTruck(Interval instant, Truck truck);

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

  List<DailySchedule> findSchedules(String truckId, DateTime start, DateTime end);

  /**
   * Finds the set of trucks that will be at a location on a particular date
   * @param localDate the date
   * @param location the location
   * @return the set of trucks that will be there at that date
   */
  Set<Truck> findTrucksAtLocation(LocalDate localDate, Location location);

  /**
   * Finds trucks within 1/5 of a mile of the specified location
   * @param location the location to search on
   * @param currentTime the time of day that the
   * @return The set of trucks that fit the search criteria
   */
  Set<Truck> findTrucksNearLocation(Location location, DateTime currentTime);

  /**
   * Find all the truck stops since the specified date for the specified truck
   * @param since the lower bound date
   * @param truckId the truck ID
   * @return the list of all truck stops
   */
  List<TruckStop> findStopsForTruckSince(DateTime since, String truckId);

  /**
   * Generates a weekly schedule based on popular stops/
   * @return the weekly schedule for the current week
   */
  WeeklySchedule findPopularStopsForWeek(LocalDate startDate);
}
