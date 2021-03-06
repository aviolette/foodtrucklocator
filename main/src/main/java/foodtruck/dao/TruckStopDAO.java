package foodtruck.dao;

import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public interface TruckStopDAO extends DAO<Long,TruckStop> {
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
   * Deletes the stops after the specified time
   */
  void deleteStops(List<TruckStop> toDelete);

  /**
   * Returns all the truck stops over a specific time range
   * @param truckId the truck ID
   * @param range the range to search over
   * @return the list of all the truck stops (sorted ascending by time)
   */
  List<TruckStop> findOverRange(@Nullable String truckId, Interval range);

  @Nullable TruckStop findFirstStop(String truckId);

  List<TruckStop> findAfter(String truckId, DateTime startTime);

  List<TruckStop> findAfter(DateTime startTime);

  List<TruckStop> findVendorStopsAfter(DateTime start, String truckId);

  List<TruckStop> findVendorStopsAfter(DateTime start);
}
