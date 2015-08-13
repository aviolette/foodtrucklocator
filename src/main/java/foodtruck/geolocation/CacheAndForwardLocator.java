package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import org.joda.time.DateTime;

import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.util.Clock;

/**
 * A geo-locator that attempts to find a keyword in the datastore, if not it delegates
 * to another geolocator to perform the lookup.
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class CacheAndForwardLocator implements GeoLocator {
  private final LocationDAO dao;
  private final GeoLocator secondaryLocator;
  private static final Logger log = Logger.getLogger(CacheAndForwardLocator.class.getName());
  private FifteenMinuteRollupDAO monitor;
  private Clock clock;
  private final static String LAT_LNG = "(\\+|-)?[\\d|\\.]+,(\\+|-)?[\\d|\\.]+";

  @Inject
  public CacheAndForwardLocator(LocationDAO dao, @SecondaryGeolocator GeoLocator secondaryLocator,
      FifteenMinuteRollupDAO monitor, Clock clock) {
    this.secondaryLocator = secondaryLocator;
    this.dao = dao;
    this.monitor = monitor;
    this.clock = clock;
  }

  @Override
  public @Nullable Location locate(String location, GeolocationGranularity granularity) {
    location = location.trim();
    if (location.matches(LAT_LNG)) {
      String[] latLng = location.split(",");
      final double latitude = Double.parseDouble(latLng[0]);
      final double longitude = Double.parseDouble(latLng[1]);
      Location foundLocation = reverseLookup(Location.builder().name(location)
          .lat(latitude).lng(longitude).build());
      log.log(Level.INFO, "Reverse lookup location: {0}", foundLocation);
      return foundLocation;
    }
    DateTime now = clock.now();
    monitor.updateCount(now, "cacheLookup_total");
    Location loc = dao.findByAlias(location);
    if (loc != null) {
      return loc;
    } else {
      monitor.updateCount(now, "cacheLookup_failed");
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
    return dao.saveAndFetch(loc).wasJustResolved();
  }

  @Override
  public @Nullable Location reverseLookup(Location location) {
    // TODO: lookup address in cache (can we do a radius search?)
    for (Location loc : dao.findPopularLocations()) {
      if (location.containedWithRadiusOf(loc)) {
        return location.withName(loc.getName());
      }
    }
    try {
      // TODO: in the case where the result does not equal the default value, save location to DB
      log.log(Level.INFO, "Looking up location: {0}", location);
      return secondaryLocator.reverseLookup(location);
    } catch (UnsupportedOperationException use) {
      return null;
    }
  }
}
