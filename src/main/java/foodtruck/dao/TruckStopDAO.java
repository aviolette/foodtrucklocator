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

  void update(TruckStop truckStop);
}
