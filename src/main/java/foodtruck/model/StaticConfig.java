package foodtruck.model;

/**
 * @author aviolette
 * @since 1/5/15
 */
public class StaticConfig {
  public String getSignalId() {
    return System.getProperty("foodtrucklocator.signal.id");
  }

  public String getState() {
    return System.getProperty("foodtrucklocator.state", "IL");
  }

  public String getCity() {
    return System.getProperty("foodtrucklocator.city", "Chicago");
  }

  public String getCityState() {
    return getCity() + ", " + getState();
  }
}
