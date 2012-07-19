package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;
import foodtruck.model.Location;
import foodtruck.util.Clock;
import foodtruck.util.ServiceException;

/**
 * @author aviolette@gmail.com
 * @since 4/11/12
 */
public class OrderedGeolocator implements GeoLocator {
  private GoogleGeolocator googleGeolocator;
  private YahooGeolocator yahooGeolocator;
  private ConfigurationDAO configurationDAO;
  private static final Logger log = Logger.getLogger(OrderedGeolocator.class.getName());
  private final Clock clock;

  @Inject
  public OrderedGeolocator(GoogleGeolocator googleGeolocator, YahooGeolocator yahooGeolocator,
      ConfigurationDAO configurationDAO, Clock clock) {
    this.googleGeolocator = googleGeolocator;
    this.yahooGeolocator = yahooGeolocator;
    this.configurationDAO = configurationDAO;
    this.clock = clock;
  }

  @Override
  public Location locate(String location, GeolocationGranularity granularity) {
    Configuration config = configurationDAO.findSingleton();
    if (config.isGoogleGeolocationEnabled() && !config.isGoogleThrottled(clock.now())) {
      try {
        Location loc = googleGeolocator.locate(location, granularity);
        if (loc != null) {
          return loc;
        }
      } catch (ServiceException serviceException) {
        log.log(Level.WARNING, serviceException.getMessage(), serviceException);
      }
    } else {
      log.log(Level.INFO, "Skipping google for geolocation. Throttle value: {0}",
          config.getThrottleGoogleUntil());
    }
    if (config.isYahooGeolocationEnabled()) {
      return yahooGeolocator.locate(location, granularity);
    }
    return null;
  }

  @Override public @Nullable Location reverseLookup(Location location) {
    Configuration config = configurationDAO.findSingleton();
    if (config.isGoogleGeolocationEnabled() && !config.isGoogleThrottled(clock.now())) {
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
    if (config.isYahooGeolocationEnabled()) {
      return yahooGeolocator.reverseLookup(location);
    }
    return null;
  }
}
