package foodtruck.monitoring;

/**
 * @author aviolette
 * @since 11/30/16
 */
public interface Counter {
  /**
   * Gets the count for the specified item.
   * @param suffix a suffix that identifies the item that is being counted
   * @return the count
   */
  long getCount(String suffix);

  /**
   * Increments the count.
   * @param suffix the identifier
   */
  void increment(String suffix);

  /**
   * Clears the count.
   * @param suffix the identifier
   */
  void clear(String suffix);
}
