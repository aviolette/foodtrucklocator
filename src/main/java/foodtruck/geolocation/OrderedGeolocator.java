package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;
import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since 4/11/12
 */
public class OrderedGeolocator implements GeoLocator {
  private GoogleGeolocator googleGeolocator;
  private YahooGeolocator yahooGeolocator;
  private ConfigurationDAO configurationDAO;
  private static final Logger log = Logger.getLogger(OrderedGeolocator.class.getName());

  @Inject
  public OrderedGeolocator(GoogleGeolocator googleGeolocator, YahooGeolocator yahooGeolocator,
      ConfigurationDAO configurationDAO) {
    this.googleGeolocator = googleGeolocator;
    this.yahooGeolocator = yahooGeolocator;
    this.configurationDAO = configurationDAO;
  }

  @Override
  public Location locate(String location, GeolocationGranularity granularity) {
    Configuration config = configurationDAO.findSingleton();
    if (config.isGoogleGeolocationEnabled()) {
      Location loc = googleGeolocator.locate(location, granularity);
      if (loc != null) {
        return loc;
      }
    }
    if (config.isYahooGeolocationEnabled()) {
      return yahooGeolocator.locate(location, granularity);
    }
    return null;
  }

  @Override
  public String reverseLookup(Location location, String defaultValue) {
    Configuration config = configurationDAO.findSingleton();
    if (config.isGoogleGeolocationEnabled()) {
      log.log(Level.INFO, "Looking up location: {0}", location);
      return googleGeolocator.reverseLookup(location, defaultValue);
    } else {
      return defaultValue;
    }
  }
}
