package foodtruck.dao;

import java.util.Collection;
import java.util.List;

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
  @Nullable Location findByAddress(String keyword);

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

  Collection<Location> findByTwitterId(String twitterId);

  Collection<Location> findByManagerEmail(String email);

  List<Location> findAlexaStops();
}