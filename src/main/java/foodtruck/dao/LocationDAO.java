package foodtruck.dao;

import java.util.Set;

import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public interface LocationDAO {
  /**
   * Looks up a set of location based on the keywords.
   * @return A set of locations that match the keyword (or an empty set).
   */
  Set<Location> lookup(String keyword);
}
