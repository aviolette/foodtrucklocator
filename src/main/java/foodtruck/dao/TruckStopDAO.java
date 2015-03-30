package foodtruck.dao;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public interface TruckStopDAO {
  /**
   * Finds a set of truck stops at a particular time
   */
  Set<TruckStop> findAt(DateTime instant);

  /**
   * Adds the truckstops to the data store.
   */
  void addStops(List<TruckStop> stops);

  /**
   * Returns the truck stops during the specified day.
   * @param truckId the truckId
   * @param day the day to search over
   * @return the list of truck stops, sorted by start time
   * @throws IllegalStateException if the truck specified is invalid
   */
  List<TruckStop> findDuring(@Nullable String truckId, LocalDate day);

  /**
   * Deletes all the stops after the specified time
   * @param startDateTime the start time
   */
  void deleteAfter(DateTime startDateTime);

  /**
   * Deletes all the stops after the specified time for the specified truck.
   * @param dateTime the time to start
   * @param truckId for the specified truck
   */
  void deleteAfter(DateTime dateTime, String truckId);

  /**
   * Deletes the stops after the specified time
   */
  void deleteStops(List<TruckStop> toDelete);

  @Nullable TruckStop findById(long stopId);

  /**
   * Deletes the truck stop specified by the ID
   * @param stopId
   */
  void delete(long stopId);

  void save(TruckStop truckStop);

  /**
   * Returns all the truck stops over a specific time range
   * @param truckId the truck ID
   * @param range the range to search over
   * @return the list of all the truck stops (sorted ascending by time)
   */
  List<TruckStop> findOverRange(@Nullable String truckId, Interval range);

  TruckStop findFirstStop(String id);

  List<TruckStop> findAfter(String truckId, DateTime startTime);
}
