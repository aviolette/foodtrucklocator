package foodtruck.geolocation;

import java.util.Random;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import foodtruck.model.Location;

/**
 * A geolocator that randomly selects between google and yahoo geolocators.
 * @author aviolette@gmail.com
 * @since 10/16/11
 */
public class RandomGeoLocator implements GeoLocator {
  private final YahooGeolocator yahooGeolocator;
  private final GoogleGeolocator googleGeolocator;
  private final Random random;

  @Inject
  public RandomGeoLocator(GoogleGeolocator googleGeolocator, YahooGeolocator yahooGeolocator) {
    this.googleGeolocator = googleGeolocator;
    this.yahooGeolocator = yahooGeolocator;
    this.random = new Random();
  }

  @Override
  public @Nullable Location locate(String location) {
    GeoLocator primary, backup;
    if (random.nextBoolean()) {
      primary = googleGeolocator;
      backup = yahooGeolocator;
    } else {
      primary = yahooGeolocator;
      backup = googleGeolocator;
    }
    Location loc = primary.locate(location);
    if (loc == null) {
      return backup.locate(location);
    }
    return loc;
  }
}
