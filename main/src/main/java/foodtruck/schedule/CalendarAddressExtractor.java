package foodtruck.schedule;

import java.util.Optional;

import com.google.inject.Inject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;
import foodtruck.model.Truck;

/**
 * A handy class that handles both extraction of an address from a string and it's geolocation resolution.
 */
public class CalendarAddressExtractor  {

  private final AddressExtractor extractor;
  private final GeoLocator geoLocator;

  @Inject
  public CalendarAddressExtractor(AddressExtractor addressExtractor, GeoLocator geoLocator) {
    this.extractor = addressExtractor;
    this.geoLocator = geoLocator;
  }

  /**
   * Returns a location from the string if it can be extracted from the string and if it geolocates
   * @param text the string to search for a location
   * @param truck the truck
   * @return A location if one is found in the string and if it geolocates
   */
  public Optional<Location> parse(String text, Truck truck) {
    Optional<Location> locationOpt = geoLocator.locateOpt(text);
    if (locationOpt.isPresent()) {
      Location loc = locationOpt.get();
      if (loc.isResolved()) {
        return Optional.of(loc);
      }
    }

    Optional<String> locationName = extractor.parse(text, truck).stream().findFirst();
    if (locationName.isPresent()) {
      locationOpt = geoLocator.locateOpt(locationName.get());
      if (!locationOpt.isPresent()) {
        return Optional.empty();
      }
      Location loc = locationOpt.get();
      if (loc.isResolved()) {
        return Optional.of(loc);
      }
    }

    return Optional.empty();
  }
}
