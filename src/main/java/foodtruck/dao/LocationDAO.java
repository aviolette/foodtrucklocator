package foodtruck.dao;

import javax.annotation.Nullable;

import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public interface LocationDAO {
  /**
   * Looks up a location by an address.
   */
  Location lookup(String keyword);

  /**
   * Saves a location to the datastore.  Location object with new key is returned if
   * this is a new object.
   */
  Location save(Location location);

  /**
   * Record that a lookup of this location failed.
   * @deprecated Just save Location object with invalid key set.
   */
  void saveAttemptFailed(String location);

  /**
   * Finds the location by its key.
   * @param id the db id
   * @return the location or null if it cannot be found
   */
  @Nullable Location findByKey(long id);
}
