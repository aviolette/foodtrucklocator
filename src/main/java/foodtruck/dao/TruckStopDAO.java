package foodtruck.dao;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
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
   * @return the list of truck stops
   * @throws IllegalStateException if the truck specified is invalid
   */
  List<TruckStop> findDuring(@Nullable String truckId, LocalDate day);

  void deleteAfter(DateTime startDateTime);

  void deleteAfter(DateTime dateTime, String truckId);

  /**
   * Deletes the stops after the specified time
   */
  void deleteStops(List<TruckStop> toDelete);

  TruckStop findById(long stopId);

  void delete(long stopId);

  void save(TruckStop truckStop);

  /**
   * Returns all the truck stops over a specific time range
   * @param truckId the truck ID
   * @param startDate the start time (inclusive)
   * @param endDate the end time (exclusive)
   * @return the list of all the truck stops
   */
  List<TruckStop> findOverRange(@Nullable String truckId, DateTime startDate, DateTime endDate);

}
