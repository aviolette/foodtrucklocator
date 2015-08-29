package foodtruck.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since 2/26/12
 */
public interface TruckDAO extends DAO<String, Truck> {

  /**
   * Returns the trucks by their twitter handle.  It is possible for more than one truck to be
   * associated with a twitter handle
   * @param screenName screenName
   * @return the list of trucks associated with the twitter handle (or empty if none found)
   */
  Collection<Truck> findByTwitterId(String screenName);

  /**
   * Find all trucks that have not been marked inactive.
   */
  Collection<Truck> findActiveTrucks();

  /**
   * Find all trucks that have not been marked inactive.
   */
  Collection<Truck> findInactiveTrucks();

  /**
   * Find trucks with associated google calendars.
   */
  Set<Truck> findTrucksWithCalendars();

  /**
   * Find all the visible trucks, ordered by name
   */
  List<Truck> findVisibleTrucks();

  /**
   * Finds all trucks whose facebook feeds should be scanned
   */
  List<Truck> findFacebookTrucks();

  /**
   * Returns the first truck in the db
   */
  @Nullable Truck findFirst();

  /**
   * Returns a set of trucks filtered by category
   * @param tag the category
   * @return the collection of trucks
   */
  Collection<Truck> findByCategory(String tag);

  Set<Truck> findByBeaconnaiseEmail(String email);

  Iterable<Truck> findTrucksWithEmail();

  void deleteAll();
}
