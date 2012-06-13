package foodtruck.geolocation;

/**
 * Represents the level of granularity wanted out of a geolocation search.
 * @author aviolette@gmail.com
 * @since 11/21/11
 */
public enum GeolocationGranularity {
  /**
   * Matches only intersections and addresses
   */
  NARROW,
  /**
   * Matches a larger sets of places than a NARROW search
   */
  BROAD
}
