package foodtruck.dao;

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
   * Saves a location to the datastore.
   */
  void save(Location location);
}
