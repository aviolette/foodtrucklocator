package foodtruck.geolocation;

import javax.annotation.Nullable;

import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public interface GeoLocator {
  /**
   * Produces a location from the input string or {@code null} if the location cannot be found
   */
  @Nullable Location locate(String location);
}
