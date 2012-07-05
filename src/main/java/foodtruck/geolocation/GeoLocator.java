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
   * @param location the search location
   * @param granularity the level of granularity you'd like in the search
   * @throws OverQueryLimitException if the search cannot be performed because of a search quota
   * @return the geo-located location, or {@code null} if the location cannot be found
   */
  @Nullable Location locate(String location, GeolocationGranularity granularity)
      throws OverQueryLimitException;

  /**
   * Returns the name of the specified location (or defaultValue if there is none)
   * @param location the location object, populated with lat/lng
   * @param defaultValue the default value to return if the search cannot be performed
   * @throws OverQueryLimitException if the search cannot be performed because of a search quota
   * @return the name of the location, or the default value if the reverse lookup cannot be
   * processed
   */
  String reverseLookup(Location location, String defaultValue);
}
