package foodtruck.model;

/**
 * @author aviolette
 * @since 1/5/15
 */
public class StaticConfig {
  public boolean isRecachingEnabled() {
    return "true".equals(System.getProperty("foodtrucklocator.recache.enabled", "true"));
  }

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

  public String getBaseUrl() {
    return System.getProperty("foodtrucklocator.baseUrl", "http://www.chicagofoodtruckfinder.com");
  }

  public String getIconBucket() {
    return System.getProperty("foodtrucklocator.icon.bucket", "");
  }

  public String getPrimaryTwitterList() {
    return System.getProperty("foodtrucklocator.twitter.list.id");
  }

  public boolean getShowTruckGraphs() {
    return "true".equals(System.getProperty("foodtrucklocator.show.truck.graphs", "true"));
  }

  public boolean isScheduleCachingOn() {
    return "true".equals(System.getProperty("foodtrucklocator.schedule.caching", "true"));
  }

  public boolean isAutoOffRoad() {
    return "true".equals(System.getProperty("foodtrucklocator.auto.off.road", "true"));
  }
}
