package foodtruck.geolocation;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;

/**
 * A cheap geolocator that resolves a pre-configured set of keywords
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class KeywordLocator implements GeoLocator {
  private final LocationDAO dao;

  @Inject
  public KeywordLocator(LocationDAO dao) {
    this.dao = dao;
  }

  @Override 
  public @Nullable Location locate(String location) {
    return Iterables.getFirst(dao.lookup(location), null);
  }
}
