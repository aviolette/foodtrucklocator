package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.util.ServiceException;

/**
 * @author aviolette@gmail.com
 * @since 4/11/12
 */
public class OrderedGeolocator implements GeoLocator {
  private static final Logger log = Logger.getLogger(OrderedGeolocator.class.getName());
  private final StaticConfig staticConfig;
  private final GoogleGeolocator googleGeolocator;
  private final YQLGeolocator yahooGeolocator;

  @Inject
  public OrderedGeolocator(GoogleGeolocator googleGeolocator, YQLGeolocator yahooGeolocator,
      StaticConfig staticConfig) {
    this.googleGeolocator = googleGeolocator;
    this.yahooGeolocator = yahooGeolocator;
    this.staticConfig = staticConfig;
  }

  @Override
  public Location locate(String location, GeolocationGranularity granularity) {
    // TODO: is throttling even necessary? If so, pull it out of config
    if (staticConfig.isGoogleGeolocationEnabled()) {
      try {
        Location loc = googleGeolocator.locate(location, granularity);
        if (loc != null) {
          return loc;
        }
      } catch (ServiceException serviceException) {
        log.log(Level.WARNING, serviceException.getMessage(), serviceException);
      }
    }
    if (staticConfig.isYahooGeolocationEnabled()) {
      return yahooGeolocator.locate(location, granularity);
    }
    return null;
  }

  @Override
  public @Nullable Location reverseLookup(Location location) {
    if (staticConfig.isGoogleGeolocationEnabled()) {
      log.log(Level.INFO, "Looking up location: {0}", location);
      try {
        Location loc = googleGeolocator.reverseLookup(location);
        if (loc != null) {
          return loc;
        }
      } catch (ServiceException e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
    }
    if (staticConfig.isYahooGeolocationEnabled()) {
      return yahooGeolocator.reverseLookup(location);
    }
    return null;
  }
}
