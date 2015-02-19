package foodtruck.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;

import foodtruck.schedule.Confidence;

/**
 * @author aviolette@gmail.com
 * @since 4/10/12
 */
public class Configuration extends ModelEntity {
  private boolean yahooGeolocationEnabled;
  private boolean googleGeolocationEnabled;
  private DateTime throttleGoogleUntil;
  private @Nullable Location center;
  private @Nullable String yahooAppId;
  private @Nullable String googleCalendarAddress;
  private List<String> systemNotificationList;
  private @Nullable String notificationSender;
  private @Nullable String frontDoorAppKey;
  private boolean scheduleCachingOn;
  private boolean retweetStopCreatingTweets;
  private boolean sendNotificationTweetWhenNoTrucks;
  private boolean foodTruckRequestOn;
  private boolean showPublicTruckGraphs;
  private boolean autoOffRoad;
  private Confidence minimumConfidenceForDisplay;
  private String syncUrl;
  private String syncAppKey;

  public Configuration(Object key) {
    super(key);
  }

  private Configuration(Builder builder) {
    this(builder.key);
    this.yahooGeolocationEnabled = builder.yahooGeolocationEnabled;
    this.throttleGoogleUntil = builder.throttleGoogleUntil;
    this.center = builder.center;
    this.yahooAppId = builder.yahooAppId;
    this.googleCalendarAddress = builder.googleCalendarAddress;
    this.systemNotificationList = builder.systemNotificationList;
    this.notificationSender = builder.notificationSender;
    this.frontDoorAppKey = builder.frontDoorAppKey;
    this.scheduleCachingOn = builder.scheduleCachingOn;
    this.retweetStopCreatingTweets = builder.retweetStopCreatingTweets;
    this.sendNotificationTweetWhenNoTrucks = builder.sendNotificationTweetWhenNoTrucks;
    this.foodTruckRequestOn = builder.foodTruckRequestOn;
    this.showPublicTruckGraphs = builder.showPublicTruckGraphs;
    this.autoOffRoad = builder.autoOffRoad;
    this.minimumConfidenceForDisplay = builder.minimumConfidenceForDisplay;
    this.syncUrl = builder.syncUrl;
    this.syncAppKey = builder.syncAppKey;
    this.googleGeolocationEnabled = builder.googleGeolocationEnabled;
  }

  public Confidence getMinimumConfidenceForDisplay() {
    return minimumConfidenceForDisplay;
  }

  public String getDisplayConfidenceMinimum() {
    return minimumConfidenceForDisplay.toString();
  }

  public boolean isAutoOffRoad() {
    return autoOffRoad;
  }

  public boolean isShowPublicTruckGraphs() {
    return this.showPublicTruckGraphs;
  }

  public boolean isSendNotificationTweetWhenNoTrucks() {
    return this.sendNotificationTweetWhenNoTrucks;
  }

  public boolean isFoodTruckRequestOn() {
    return this.foodTruckRequestOn;
  }

  @Nullable public String getFrontDoorAppKey() {
    return frontDoorAppKey;
  }

  public List<String> getSystemNotificationList() {
    return systemNotificationList;
  }

  public String getNotificationReceivers() {
    return Joiner.on(",").join(systemNotificationList);
  }

  public @Nullable String getGoogleCalendarAddress() {
    return this.googleCalendarAddress;
  }

  public @Nullable String getYahooAppId() {
    return this.yahooAppId;
  }

  public boolean isYahooGeolocationEnabled() {
    return yahooGeolocationEnabled;
  }

  public boolean isGoogleGeolocationEnabled() {
    return googleGeolocationEnabled;
  }

  public boolean isGoogleThrottled(DateTime when) {
    return (throttleGoogleUntil != null && throttleGoogleUntil.isAfter(when));
  }

  public @Nullable Location getCenter() {
    return center;
  }

  public @Nullable DateTime getThrottleGoogleUntil() {
    return throttleGoogleUntil;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getNotificationSender() {
    return notificationSender;
  }

  public static Builder builder(Configuration config) {
    return new Builder(config);
  }

  public boolean isScheduleCachingOn() {
    return scheduleCachingOn;
  }

  public boolean isRetweetStopCreatingTweets() {
    return retweetStopCreatingTweets;
  }

  public String getSyncUrl() {
    return syncUrl;
  }

  public String getSyncAppKey() {
    return syncAppKey;
  }

  public static class Builder {
    private boolean googleGeolocationEnabled = true;
    private boolean yahooGeolocationEnabled = false;
    private Key key;
    private @Nullable DateTime throttleGoogleUntil;
    private Location center;
    private @Nullable String yahooAppId;
    private @Nullable String googleCalendarAddress;
    private List<String> systemNotificationList = ImmutableList.of();
    private @Nullable String notificationSender;
    private @Nullable String frontDoorAppKey;
    private boolean scheduleCachingOn;
    private boolean retweetStopCreatingTweets;
    private boolean sendNotificationTweetWhenNoTrucks;
    private boolean foodTruckRequestOn = true;
    private boolean showPublicTruckGraphs = true;
    private boolean autoOffRoad;
    private Confidence minimumConfidenceForDisplay = Confidence.HIGH;
    private String syncAppKey;
    private String syncUrl;

    public Builder() {
    }

    public Builder(Configuration config) {
      this.syncUrl = config.syncUrl;
      this.syncAppKey = config.syncAppKey;
      this.yahooGeolocationEnabled = config.isYahooGeolocationEnabled();
      this.throttleGoogleUntil = config.throttleGoogleUntil;
      this.key = (Key) config.getKey();
      this.yahooAppId = config.yahooAppId;
      this.googleCalendarAddress = config.googleCalendarAddress;
      this.systemNotificationList = config.systemNotificationList;
      this.notificationSender = config.notificationSender;
      this.frontDoorAppKey = config.frontDoorAppKey;
      this.scheduleCachingOn = config.scheduleCachingOn;
      this.sendNotificationTweetWhenNoTrucks = config.sendNotificationTweetWhenNoTrucks;
      this.foodTruckRequestOn = config.foodTruckRequestOn;
      this.showPublicTruckGraphs = config.showPublicTruckGraphs;
      this.autoOffRoad = config.autoOffRoad;
    }

    public Builder syncAppKey(String syncAppKey) {
      this.syncAppKey = syncAppKey;
      return this;
    }

    public Builder syncUrl(String syncUrl) {
      this.syncUrl = syncUrl;
      return this;
    }

    public Builder autoOffRoad(boolean autoOffRoad) {
      this.autoOffRoad = autoOffRoad;
      return this;
    }

    public Builder showPublicTruckGraphs(boolean showPublicTruckGraphs) {
      this.showPublicTruckGraphs = showPublicTruckGraphs;
      return this;
    }

    public Builder foodTruckRequestOn(boolean foodTruckRequestOn) {
      this.foodTruckRequestOn = foodTruckRequestOn;
      return this;
    }

    public Builder sendNotificationTweetWhenNoTrucks(boolean sendNotification) {
      this.sendNotificationTweetWhenNoTrucks = sendNotification;
      return this;
    }

    public Builder scheduleCachingOn(boolean scheduleCachingOn) {
      this.scheduleCachingOn = scheduleCachingOn;
      return this;
    }

    public Builder frontDoorAppKey(String frontDoorAppKey) {
      this.frontDoorAppKey = frontDoorAppKey;
      return this;
    }

    public Builder notificationSender(String notificationSender) {
      this.notificationSender = notificationSender;
      return this;
    }

    public Builder googleCalendarAddress(String address) {
      this.googleCalendarAddress = address;
      return this;
    }

    public Builder yahooAppId(String yahooAppId) {
      this.yahooAppId = yahooAppId;
      return this;
    }

    public Builder googleGeolocationEnabled(boolean enabled) {
      this.googleGeolocationEnabled = enabled;
      return this;
    }

    public Builder yahooGeolocationEnabled(boolean enabled) {
      this.yahooGeolocationEnabled = enabled;
      return this;
    }

    public Builder systemNotificationList(List<String> systemNotificationList) {
      this.systemNotificationList = ImmutableList.copyOf(systemNotificationList);
      return this;
    }

    public Builder center(@Nullable Location location) {
      this.center = location;
      return this;
    }

    public Builder key(Key key) {
      this.key = key;
      return this;
    }

    public Configuration build() {
      return new Configuration(this);
    }

    public Builder throttleGoogleGeocoding(DateTime dateTime) {
      this.throttleGoogleUntil = dateTime;
      return this;
    }

    public Builder retweetStopCreatingTweets(boolean retweet) {
      this.retweetStopCreatingTweets = retweet;
      return this;
    }

    public Builder minimumConfidenceForDisplay(Confidence confidence) {
      this.minimumConfidenceForDisplay = confidence;
      return this;
    }
  }
}
