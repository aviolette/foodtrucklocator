package foodtruck.geolocation;

import java.util.logging.Logger;

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
  private static final Logger log = Logger.getLogger(CacheAndStoreLocator.class.getName());

  @Inject
  public CacheAndStoreLocator(LocationDAO dao,
      @SecondaryGeolocator GeoLocator secondaryLocator) {
    this.secondaryLocator = secondaryLocator;
    this.dao = dao;
  }

  @Override
  public @Nullable Location locate(String location, GeolocationGranularity granularity) {
    Location loc = dao.lookup(location);
    if (loc != null) {
      // there were previous attempts at using the secondary locator which were unsuccessful
      // so don't try again.
      return loc.isResolved() ? loc : null;
    }
    loc = secondaryLocator.locate(location, granularity);
    if (loc != null) {
      dao.save(loc);
    } else {
      // write that we tried to save this location so that we don't try again.
      log.warning("Failed at attempt to geo locate: " + location);
      dao.saveAttemptFailed(location);
    }
    return loc;
  }
}
