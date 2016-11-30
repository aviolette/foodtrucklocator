package foodtruck.monitoring;

/**
 * @author aviolette
 * @since 11/30/16
 */
public interface Counter {

  long getCount(String suffix);

  void increment(String suffix);

  void clear(String suffix);
}
