package foodtruck.monitoring;

/**
 * @author aviolette
 * @since 11/30/16
 */
public interface CounterPublisher {
  void increment(String name);
}
