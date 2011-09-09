package foodtruck.geolocation;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;

/**
 * A geo-locator that attempts to find a keyword in the datastore, if not it delegates
 * to another geolocator to perform the lookup.
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class CacheAndStoreLocator implements GeoLocator {
  private final LocationDAO dao;
  private final GeoLocator secondaryLocator;

  @Inject
  public CacheAndStoreLocator(LocationDAO dao,
      @SecondaryGeolocator GeoLocator secondaryLocator) {
    this.secondaryLocator = secondaryLocator;
    this.dao = dao;
  }

  @Override
  public @Nullable Location locate(String location) {
    Location loc = dao.lookup(location);
    if (loc != null) {
      return loc;
    }
    loc = secondaryLocator.locate(location);
    // only update named locations in the db
    if (loc != null && loc.isNamed()) {
      dao.save(loc);
    }
    return loc;
  }
}
