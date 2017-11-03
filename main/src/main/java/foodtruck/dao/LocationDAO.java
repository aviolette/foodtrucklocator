package foodtruck.dao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public interface LocationDAO extends DAO<Long, Location> {
  /**
   * Looks up a location by an address.
   *
   * @param keyword an address to search on
   * @return the location or null if it could not be found
   */
  @Deprecated // use findByName
  @Nullable Location findByAddress(String keyword);

  /**
   * Looks up a location by an address.
   * @param name the address
   * @return the location
   */
  Optional<Location> findByName(String name);

  /**
   * Finds a location keyed by the specified latitude and longitude in the database.
   */
  @Nullable Location findByLatLng(Location location);

  /**
   * Saves a location to the datastore.  Location object with new key is returned if
   * this is a new object.
   *
   * @param location the location to save
   * @return the location object with a new key
   */
  Location saveAndFetch(Location location);

  /**
   * Finds all the popular locations, sorted by name (ascending)
   */
  List<Location> findPopularLocations();

  /**
   * Finds all the locations that should be in the autocomplete list, sorted by name
   */
  List<Location> findAutocompleteLocations();

  /**
   * A convenience function for converting auto-complete locations to json string
   */
  String findLocationNamesAsJson();

  List<Location> findLocationsOwnedByFoodTrucks();

  /**
   * Finds all alias for a specified location, sorted by name (ascending)
   */
  List<Location> findAliasesFor(String locationName);

  /**
   * Finds all the locations marked as a designated stop
   */
  Collection<Location> findDesignatedStops();

  Iterable<Location> findBoozyLocations();

  Location findByAlias(String location);

  List<Location> findAlexaStops();
}
