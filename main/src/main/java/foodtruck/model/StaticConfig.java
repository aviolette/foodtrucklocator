package foodtruck.model;

import java.util.Iterator;

import com.google.common.base.Splitter;

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

  public boolean getSupportsBooking() {
    return "true".equals(System.getProperty("foodtrucklocator.supports.booking"));
  }

  public String getUserAgent() {
    return System.getProperty("foodtrucklocator.user.agent", "ChicagoFoodTruckFinder");
  }

  public String getSlackRedirect() {
    return getBaseUrl() + "/slack/oauth";
  }
}
