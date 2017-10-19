package foodtruck.monitoring;

/**
 * @author aviolette
 * @since 11/30/16
 */
public interface CounterPublisher {
  /**
   * Increment a counter by one
   * @param name the name of the counter
   */
  void increment(String name);

  /**
   * Increment a count by the specified amount
   * @param name the name
   * @param amount a positive integer
   */
  void increment(String name, int amount);
}
