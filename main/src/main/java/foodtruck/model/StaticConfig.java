package foodtruck.model;

/**
 * @author aviolette
 * @since 1/5/15
 */
public class StaticConfig {


  public String getBaseUrl() {
    return System.getProperty("foodtrucklocator.baseUrl", "http://www.chicagofoodtruckfinder.com");
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
