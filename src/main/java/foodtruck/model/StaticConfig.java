package foodtruck.model;

/**
 * @author aviolette
 * @since 1/5/15
 */
public class StaticConfig {
  public String getSignalId() {
    return System.getProperty("foodtrucklocator.signal.id");
  }
}
