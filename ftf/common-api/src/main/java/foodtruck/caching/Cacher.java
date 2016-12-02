package foodtruck.caching;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 12/2/16
 */
public interface Cacher {
  /**
   * Puts the object in the cache with an expiration measured in minutes
   * @param key the key
   * @param value the value
   * @param minutes number of minutes until expiration
   */
  void put(String key, @Nullable Object value, int minutes);

  /**
   * Puts the object in the cache, with an expiry determined by the specified date
   * @param key the key
   * @param value the value
   * @param expirationDate the datetime that it expires on
   */
  void put(String key, @Nullable Object value, DateTime expirationDate);

  /**
   * Returns the object specified by the key or null if it is not specified.
   * @param key the key
   * @return the object or null if it is not in the cache
   */
  @Nullable
  Object get(String key);

  boolean contains(String key);

  boolean delete(String key);
}
