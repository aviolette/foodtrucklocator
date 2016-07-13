package foodtruck.truckstops;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.beaconnaise.BeaconSignal;
import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStatus;
import foodtruck.model.TruckStop;
import foodtruck.model.TruckStopWithCounts;

/**
 * Service for manipulating food truck stops.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public interface FoodTruckStopService {
  /**
   * Syncs the custom calendar stops (removes old entries, and adds new ones)
   * @param range the time range to update
   */
  void pullCustomCalendars(Interval range);

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

  void pullCustomCalendarFor(Interval instant, Truck truck);

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
  void update(TruckStop truckStop, String modifier);

  List<TruckStatus> findCurrentAndPreviousStop(LocalDate day);

  /**
   * Syncs the location (based on name) to all the truck stops on the current day.
   */
  void updateLocationInCurrentSchedule(Location location);

  /**
   * Finds the daily schedules over a particular range
   * @param truckId the truck ID
   * @param range the range to find the schedules
   * @return the schedules
   */
  List<DailySchedule> findSchedules(String truckId, Interval range);

  /**
   * Finds trucks within 1/5 of a mile of the specified location
   * @param location the location to search on
   * @param currentTime the time of day that the
   * @return The set of trucks that fit the search criteria
   */
  Set<Truck> findTrucksNearLocation(Location location, DateTime currentTime);

  /**
   * Finds all the truck stops near a location for a a day
   * @param location the location
   * @param theDate the date
   * @return the truck stops in temporal order
   */
  List<TruckStop> findStopsNearALocation(Location location, LocalDate theDate);

  /**
   * Find all the truck stops since the specified date for the specified truck
   * @param since the lower bound date
   * @param truckId the truck ID
   * @return the list of all truck stops
   */
  List<TruckStop> findStopsForTruckSince(DateTime since, String truckId);

  /**
   * Returns the daily schedule with only the stops that end after the specified time
   * @param dateTime all stops that end before this time will be filtered out
   * @return the filtered dailyschedule
   */
  DailySchedule findStopsForDayAfter(DateTime dateTime);

  /**
   * Either updates an existing truckstop or creates a new one, based on the signal
   * @param signal the beacon signal
   * @return a new or existing truck stop created from the beacon
   */
  TruckStop handleBeacon(BeaconSignal signal);

  /**
   * Takes a truck off the road (mutes it for the day and removes its stops)
   * @param truckId the truck ID
   * @param localDate the current date
   */
  void offRoad(String truckId, LocalDate localDate);

  /**
   * Similar to offTheRoad, but only removes stops that start after the current time.  It also caps any stop that is
   * active at the specified time.
   * @return the number of stops removed
   */
  int cancelRemainingStops(String truckId, DateTime after);

  TruckStop findFirstStop(Truck truck);

  /**
   * Find upcoming events that blend food trucks and booze.
   * @param localDate the start date
   * @param daysOut how many days out to search
   * @return the upcoming boozy events
   */
  List<TruckStop> findUpcomingBoozyStops(LocalDate localDate, int daysOut);

  List<TruckStopWithCounts> findStopsForTruckAfter(String truckId, DateTime startTime);

  List<DailySchedule> findStopsNearLocationOverRange(Location location, Interval range);

  List<TruckStop> findStopsAtLocationOverRange(Location location, Interval range);

}
