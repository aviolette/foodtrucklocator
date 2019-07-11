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
  List<Truck> findActiveTrucks();

  /**
   * Find all trucks that have not been marked inactive.
   */
  Collection<Truck> findInactiveTrucks();

  /**
   * Find trucks with associated google calendars.
   */
  Set<Truck> findTrucksWithCalendars();

  /**
   * Finds trucks with an associated drupal calendar
   */
  Set<Truck> findTruckWithDrupalCalendars();

  /**
   * Finds trucks with an associated drupal calendar
   */
  Set<Truck> findTruckWithICalCalendars();

  Set<Truck> findTruckWithSquarespaceCalendars();

  /**
   * Find all the visible trucks, ordered by name
   */
  List<Truck> findVisibleTrucks();

  /**
   * Returns the first truck in the db
   */
  @Nullable Truck findFirst();

  /**
   * Returns a set of trucks filtered by category
   * @param tag the category
   * @return the collection of trucks
   */
  List<Truck> findByCategory(String tag);

  Set<Truck> findByBeaconnaiseEmail(String email);

  Iterable<Truck> findTrucksWithEmail();

  void deleteAll();

  @Nullable Truck findByName(String name);

  @Nullable Truck findByNameOrAlias(String name);
}
