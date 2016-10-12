package foodtruck.model;

import java.util.Iterator;

import com.google.common.base.Splitter;

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

  public String getPrimaryTwitterListSlug() {
    return System.getProperty("foodtrucklocator.twitter.list.slug");
  }

  public String getPrimaryTwitterListOwner() {
    return System.getProperty("foodtrucklocator.twitter.list.owner");
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

  public boolean isGoogleGeolocationEnabled() {
    return "true".equals(System.getProperty("foodtrucklocator.google.geolocation", "true"));
  }

  public boolean isYahooGeolocationEnabled() {
    return "true".equals(System.getProperty("foodtrucklocator.yahoo.geolocation", "true"));
  }

  public String getSyncUrl() {
    return System.getProperty("foodtrucklocator.sync.url", null);
  }

  public String getSyncAppKey() {
    return System.getProperty("foodtrucklocator.sync.app.key", null);
  }

  public String getYahooAppId() {
    return System.getProperty("foodtrucklocator.yahoo.key");
  }

  public Location getCenter() {
    Location.Builder builder = Location.builder().name("Unnamed")
        .lat(41.880187)
        .lng(-87.63083499999999);
    try {
      Iterable<String> items = Splitter.on(";")
          .trimResults()
          .split(System.getProperty("foodtrucklocator.center", "Clark and Monroe, Chicago, IL; 41.880187; -87.63083499999999"));
      Iterator<String> it = items.iterator();
      builder.name(it.next());
      builder.lat(Double.parseDouble(it.next()));
      builder.lng(Double.parseDouble(it.next()));
    } catch (Exception ignored) {
    }
    return builder.build();
  }

  public String getFrontDoorAppKey() {
    return System.getProperty("foodtrucklocator.frontdoor.app.key", "");
  }

  public boolean showLocationGraphs() {
    return "true".equals(System.getProperty("foodtrucklocator.show.location.graphs", "true"));
  }

  public Iterable<String> getSystemNotificationList() {
    return Splitter.on(",").omitEmptyStrings()
        .split(System.getProperty("foodtrucklocator.mail.receivers", ""));
  }

  public String getNotificationSender() {
    return System.getProperty("foodtrucklocator.mail.sender", "");
  }

  public String getFacebookAccessToken() {
    return System.getProperty("foodtrucklocator.fb.access.token", null);
  }

  public String getGoogleJavascriptApiKey() {
    return System.getProperty("foodtrucklocator.google.javascript.api.key", null);
  }
}
