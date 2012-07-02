package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;

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
  private static final Logger log = Logger.getLogger(CacheAndStoreLocator.class.getName());

  @Inject
  public CacheAndStoreLocator(LocationDAO dao,
      @SecondaryGeolocator GeoLocator secondaryLocator) {
    this.secondaryLocator = secondaryLocator;
    this.dao = dao;
  }

  @Override
  public Location locate(String location, GeolocationGranularity granularity) {
    Location loc = dao.findByAddress(location);
    if (loc != null) {
      return loc;
    }
    try {
      loc = secondaryLocator.locate(location, granularity);
    } catch (Exception io) {
      log.log(Level.WARNING, io.getMessage(), io);
      loc = null;
    }
    if (loc == null) {
      loc = Location.builder().name(location).valid(false).build();
      log.warning("Failed at attempt to geo locate: " + location);
    }
    return dao.saveAndFetch(loc);
  }

  @Override
  public String reverseLookup(Location location, String defaultValue) {
    // TODO: lookup address in cache (can we do a radius search?)
    try {
      // TODO: in the case where the result does not equal the default value, save location to DB
      log.log(Level.INFO, "Looking up location: {0}", location);
      return secondaryLocator.reverseLookup(location, defaultValue);
    } catch (UnsupportedOperationException use) {
      return defaultValue;
    }
  }
}
