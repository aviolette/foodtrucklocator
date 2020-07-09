package foodtruck.model;

/**
 * @author aviolette
 * @since 1/5/15
 */
public class StaticConfig {

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
    return "truckicons";
  }

  public String getPrimaryTwitterList() {
    return System.getProperty("foodtrucklocator.twitter.list.id");
  }

  public String getPrimaryTwitterListSlug() {
    return System.getProperty("foodtrucklocator.twitter.list.slug");
  }

  public String getPrimaryTwitterListOwner() {
    return System.getProperty("foodtrucklocator.twitter.list.owner");
  }

  public boolean isScheduleCachingOn() {
    return "true".equals(System.getProperty("foodtrucklocator.schedule.caching", "true"));
  }

  public boolean isAutoOffRoad() {
    return "true".equals(System.getProperty("foodtrucklocator.auto.off.road", "true"));
  }

  public String getSyncUrl() {
    return System.getProperty("foodtrucklocator.sync.url", null);
  }

  public String getSyncAppKey() {
    return System.getProperty("foodtrucklocator.sync.app.key", null);
  }

  public String getFrontDoorAppKey() {
    return System.getProperty("foodtrucklocator.frontdoor.app.key", "");
  }

  public String getUserAgent() {
    return System.getProperty("foodtrucklocator.user.agent", "ChicagoFoodTruckFinder");
  }

  public String getSlackRedirect() {
    return getBaseUrl() + "/slack/oauth";
  }
}
