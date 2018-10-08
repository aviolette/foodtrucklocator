package foodtruck.geolocation;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.ReverseLookupDAO;
import foodtruck.model.Location;
import foodtruck.model.PartialLocation;
import foodtruck.monitoring.CounterPublisher;

/**
 * A geo-locator that attempts to find a keyword in the datastore, if not it delegates to another geolocator to perform
 * the lookup.
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
class CacheAndForwardLocator implements GeoLocator {

  private static final Logger log = Logger.getLogger(CacheAndForwardLocator.class.getName());
  private final static String LAT_LNG = "(\\+|-)?[\\d|\\.]+,(\\+|-)?[\\d|\\.]+";
  private final LocationDAO dao;
  private final GeoLocator secondaryLocator;
  private final CounterPublisher counterPublisher;
  private final ReverseLookupDAO reverseLookupDAO;

  @Inject
  public CacheAndForwardLocator(LocationDAO dao, @SecondaryGeolocator GeoLocator secondaryLocator,
      CounterPublisher counterPublisher, ReverseLookupDAO reverseLookupDAO) {
    this.secondaryLocator = secondaryLocator;
    this.dao = dao;
    this.counterPublisher = counterPublisher;
    this.reverseLookupDAO = reverseLookupDAO;
  }

  @Override
  @Nullable
  public Location locate(String location, GeolocationGranularity granularity) {
    location = location.trim();
    if (location.matches(LAT_LNG)) {
      String[] latLng = location.split(",");
      final double latitude = Double.parseDouble(latLng[0]);
      final double longitude = Double.parseDouble(latLng[1]);
      Location foundLocation = reverseLookup(Location.builder()
          .name(location)
          .lat(latitude)
          .lng(longitude)
          .build());
      log.log(Level.FINE, "Reverse lookup location: {0}", foundLocation);
      return foundLocation;
    }
    counterPublisher.increment("cacheLookup_total");
    Location loc = dao.findByAlias(location);
    if (loc != null) {
      return loc;
    } else {
      counterPublisher.increment("cacheLookup_failed");
    }
    try {
      loc = secondaryLocator.locate(location, granularity);
    } catch (Exception io) {
      log.log(Level.WARNING, io.getMessage(), io);
      loc = null;
    }
    if (loc == null) {
      loc = Location.builder()
          .name(location)
          .valid(false)
          .build();
      log.warning("Failed at attempt to geo locate: " + location);
    }
    return dao.saveAndFetch(loc)
        .wasJustResolved();
  }

  @Override
  @Nullable
  public Location reverseLookup(Location location) {
    for (Location loc : dao.findPopularLocations()) {
      if (location.containedWithRadiusOf(loc)) {
        return location.withName(loc.getName());
      }
    }

    Optional<PartialLocation> locationOpt = reverseLookupDAO.findByLatLng(location.getLatitude(), location.getLongitude());
    if (locationOpt.isPresent()) {
      String name = locationOpt.get().getName();
      Location aliased = dao.findByAlias(name);
      if (aliased != null) {
        return aliased;
      }
      return location.withName(name);
    }
    try {
      log.log(Level.INFO, "Looking up location: {0}", location);
      Location loc = secondaryLocator.reverseLookup(location);
      if (loc != null) {
        loc = Location.builder(loc)
            .valid(true)
            .build();
        Location existing = dao.findByAlias(loc.getName());
        log.log(Level.INFO, "Result location: {0}\n\n {1}", new Object[] {loc, existing});
        if (existing == null) {
          log.info("Saving to DB");
          reverseLookupDAO.save(new PartialLocation(loc.getName(), loc.getLatitude(), loc.getLongitude()));
          return dao.saveAndFetch(loc)
              .wasJustResolved();
          // if there was no alias to the looked-up location the names would be same.  Return more precise location in that case
        } else if (existing.sameName(loc)) {
          log.info("Returning existing");
          return loc;
          // if there was an alias, the names would be different and we would want to pick the alias
        } else {
          log.log(Level.INFO, "Choosing existing location: {0} {1}", new Object[]{loc, existing});
          return existing;
        }
      }
    } catch (UnsupportedOperationException ignored) {
    }
    return null;
  }
}
