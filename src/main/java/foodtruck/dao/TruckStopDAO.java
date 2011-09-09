package foodtruck.dao;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

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
   * Deletes all the truck stops after the specified instant
   */
  void deleteAfter(DateTime startTime);

  /**
   * Adds the truckstops to the data store.
   */
  void addStops(List<TruckStop> stops);
}
