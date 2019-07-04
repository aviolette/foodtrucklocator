package foodtruck.schedule;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;
import foodtruck.model.Truck;

/**
 * A handy class that handles both extraction of an address from a string and it's geolocation resolution.
 */
public class CalendarAddressExtractor  {

  private static final Logger log = Logger.getLogger(CalendarAddressExtractor.class.getName());

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
    log.log(Level.INFO, "Parsing address: {0}", text);

    Optional<Location> locationOpt = geoLocator.locateOpt(text);
    if (locationOpt.isPresent()) {
      Location loc = locationOpt.get();
      if (loc.isResolved()) {
        log.info("Entire address matches a current geolocation");
        return Optional.of(loc);
      }
    }

    Optional<String> locationName = extractor.parse(text, truck).stream().findFirst();
    if (locationName.isPresent()) {
      log.log(Level.INFO, "Address {0} matches {1}", new Object[] {text, locationName.get()});
      locationOpt = geoLocator.locateOpt(locationName.get());
      if (!locationOpt.isPresent()) {
        log.info("Address could not be geo-located");
        return Optional.empty();
      }
      Location loc = locationOpt.get();
      if (loc.isResolved()) {
        log.info("Address could not be resolved");
        return Optional.of(loc);
      }
    }

    return Optional.empty();
  }
}
