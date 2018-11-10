package foodtruck.geolocation;

import java.util.Optional;

import javax.annotation.Nullable;

import foodtruck.model.Location;
import foodtruck.util.ServiceException;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public interface GeoLocator {
  /**
   * Produces a location from the input string or {@code null} if the location cannot be found
   * @param location the search location
   * @param granularity the level of granularity you'd like in the search
   * @return the geo-located location, or {@code null} if the location cannot be found
   * @throws OverQueryLimitException if the search cannot be performed because of a search quota
   */
  @Nullable Location locate(String location, GeolocationGranularity granularity)
      throws ServiceException;

  Optional<Location> locateOpt(String location);

  /**
   * Performs a reverse lookup based on latitude/longitude
   * @param location the location with lat and lng set
   * @return a new location with the location name set, or {@code null} if the lookup could not be
   *         performed
   */
  @Nullable Location reverseLookup(Location location) throws ServiceException;
}
