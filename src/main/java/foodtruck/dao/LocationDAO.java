package foodtruck.dao;

import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public interface LocationDAO extends DAO<Long, Location> {
  /**
   * Looks up a location by an address.
   */
  Location findByAddress(String keyword);

  /**
   * Saves a location to the datastore.  Location object with new key is returned if
   * this is a new object.
   */
  Location saveAndFetch(Location location);

  /**
   * Record that a lookup of this location failed.
   * @deprecated Just save Location object with invalid key set.
   */
  void saveAttemptFailed(String location);
}
