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
   * Puts an object with no specific expiration
   * @param key the key
   * @param value the value
   */
  void put(String key, @Nullable Object value);

  /**
   * Returns the object specified by the key or null if it is not specified.
   * @param key the key
   * @return the object or null if it is not in the cache
   */
  @Nullable
  Object get(String key);

  /**
   * Returns true if the cache has an object defined by the key
   * @param key the key
   * @return true if the cacher contains an object defined by the key
   */
  boolean contains(String key);

  /**
   * Deletes the object from the cache
   * @param key the key that identifies the object
   * @return true if the object was in the cache
   */
  boolean delete(String key);
}
