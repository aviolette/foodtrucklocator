package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.util.Clock;
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
  private final ConfigurationDAO configurationDAO;
  private final Clock clock;

  @Inject
  public OrderedGeolocator(GoogleGeolocator googleGeolocator, YQLGeolocator yahooGeolocator,
      ConfigurationDAO configurationDAO, Clock clock, StaticConfig staticConfig) {
    this.googleGeolocator = googleGeolocator;
    this.yahooGeolocator = yahooGeolocator;
    this.configurationDAO = configurationDAO;
    this.clock = clock;
    this.staticConfig = staticConfig;
  }

  @Override
  public Location locate(String location, GeolocationGranularity granularity) {
    Configuration config = configurationDAO.find();
    // TODO: is throttling even necessary? If so, pull it out of config
    if (staticConfig.isGoogleGeolocationEnabled() && !config.isGoogleThrottled(clock.now())) {
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
    if (staticConfig.isYahooGeolocationEnabled()) {
      return yahooGeolocator.locate(location, granularity);
    }
    return null;
  }

  @Override public @Nullable Location reverseLookup(Location location) {
    Configuration config = configurationDAO.find();
    if (staticConfig.isGoogleGeolocationEnabled() && !config.isGoogleThrottled(clock.now())) {
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
