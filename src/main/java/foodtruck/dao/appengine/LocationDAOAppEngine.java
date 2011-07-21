package foodtruck.dao.appengine;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class LocationDAOAppEngine implements LocationDAO {
  @Override
  public Set<Location> lookup(String keyword) {
    // TODO: actually use a datastore for this.
    return ImmutableSet.of();
  }
}
