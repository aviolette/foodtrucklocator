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
  private boolean tweetUpdateServletEnabled;
  private Location center;
  private boolean localTwitterCachingEnabled;
  private boolean remoteTwitterCachingEnabled;
  private @Nullable String remoteTwitterCacheAddress;
  private @Nullable String yahooAppId;
  private @Nullable String primaryTwitterList;
  private @Nullable String googleCalendarAddress;
  private @Nullable String yahooConsumerKey;
  private @Nullable String yahooConsumerSecret;
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

  public Configuration(Object key) {
    super(key);
  }

  private Configuration(Builder builder) {
    this(builder.key);
    this.googleGeolocationEnabled = builder.googleGeolocationEnabled;
    this.yahooGeolocationEnabled = builder.yahooGeolocationEnabled;
    this.throttleGoogleUntil = builder.throttleGoogleUntil;
    this.tweetUpdateServletEnabled = builder.tweetUpdateServletEnabled;
    this.center = builder.center;
    this.localTwitterCachingEnabled = builder.localTwitterCachingEnabled;
    this.remoteTwitterCacheAddress = builder.remoteTwitterCacheAddress;
    this.remoteTwitterCachingEnabled = builder.remoteTwitterCachingEnabled;
    this.yahooAppId = builder.yahooAppId;
    this.googleCalendarAddress = builder.googleCalendarAddress;
    this.primaryTwitterList = builder.primaryTwitterList;
    this.yahooConsumerKey = builder.yahooConsumerKey;
    this.yahooConsumerSecret = builder.yahooConsumerSecret;
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
    return Joiner.on(",").join(systemNotificationList).toString();
  }

  @Nullable public String getYahooConsumerKey() {
    return yahooConsumerKey;
  }

  @Nullable public String getYahooConsumerSecret() {
    return yahooConsumerSecret;
  }

  public @Nullable String getPrimaryTwitterList() {
    return primaryTwitterList;
  }

  public @Nullable String getGoogleCalendarAddress() {
    return this.googleCalendarAddress;
  }

  public @Nullable String getYahooAppId() {
    return this.yahooAppId;
  }

  public boolean isLocalTwitterCachingEnabled() {
    return localTwitterCachingEnabled;
  }

  public boolean isRemoteTwitterCachingEnabled() {
    return remoteTwitterCachingEnabled;
  }

  public @Nullable String getRemoteTwitterCacheAddress() {
    return remoteTwitterCacheAddress;
  }

  public boolean isYahooGeolocationEnabled() {
    return yahooGeolocationEnabled;
  }

  public boolean isGoogleGeolocationEnabled() {
    return googleGeolocationEnabled;
  }

  public boolean isTweetUpdateServletEnabled() {
    return tweetUpdateServletEnabled;
  }

  public boolean isGoogleThrottled(DateTime when) {
    return (throttleGoogleUntil != null && throttleGoogleUntil.isAfter(when));
  }

  public Location getCenter() {
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

  public static class Builder {
    private boolean googleGeolocationEnabled = true;
    private boolean yahooGeolocationEnabled = false;
    private Key key;
    private @Nullable DateTime throttleGoogleUntil;
    private boolean tweetUpdateServletEnabled;
    private Location center;
    private boolean localTwitterCachingEnabled;
    private @Nullable String remoteTwitterCacheAddress;
    private boolean remoteTwitterCachingEnabled;
    private @Nullable String yahooAppId;
    private @Nullable String primaryTwitterList;
    private @Nullable String googleCalendarAddress;
    private @Nullable String yahooConsumerKey;
    private @Nullable String yahooConsumerSecret;
    private List<String> systemNotificationList = ImmutableList.of();
    private @Nullable String notificationSender;
    private @Nullable String frontDoorAppKey;
    private boolean scheduleCachingOn;
    private boolean retweetStopCreatingTweets;
    public boolean sendNotificationTweetWhenNoTrucks;
    public boolean foodTruckRequestOn = true;
    public boolean showPublicTruckGraphs = true;
    public boolean autoOffRoad;
    public Confidence minimumConfidenceForDisplay = Confidence.HIGH;

    public Builder() {
    }

    public Builder(Configuration config) {
      this.googleGeolocationEnabled = config.isGoogleGeolocationEnabled();
      this.yahooGeolocationEnabled = config.isYahooGeolocationEnabled();
      this.throttleGoogleUntil = config.throttleGoogleUntil;
      this.key = (Key) config.getKey();
      this.tweetUpdateServletEnabled = config.tweetUpdateServletEnabled;
      this.localTwitterCachingEnabled = config.localTwitterCachingEnabled;
      this.remoteTwitterCacheAddress = config.remoteTwitterCacheAddress;
      this.remoteTwitterCachingEnabled = config.remoteTwitterCachingEnabled;
      this.yahooAppId = config.yahooAppId;
      this.primaryTwitterList = config.primaryTwitterList;
      this.googleCalendarAddress = config.googleCalendarAddress;
      this.yahooConsumerKey = config.yahooConsumerKey;
      this.yahooConsumerSecret = config.yahooConsumerSecret;
      this.systemNotificationList = config.systemNotificationList;
      this.notificationSender = config.notificationSender;
      this.frontDoorAppKey = config.frontDoorAppKey;
      this.scheduleCachingOn = config.scheduleCachingOn;
      this.sendNotificationTweetWhenNoTrucks = config.sendNotificationTweetWhenNoTrucks;
      this.foodTruckRequestOn = config.foodTruckRequestOn;
      this.showPublicTruckGraphs = config.showPublicTruckGraphs;
      this.autoOffRoad = config.autoOffRoad;
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

    public Builder primaryTwitterList(String list) {
      this.primaryTwitterList = list;
      return this;
    }

    public Builder yahooAppId(String yahooAppId) {
      this.yahooAppId = yahooAppId;
      return this;
    }

    public Builder localTwitterCachingEnabled(boolean enabled) {
      this.localTwitterCachingEnabled = enabled;
      return this;
    }

    public Builder remoteTwitterCachingEnabled(boolean enabled) {
      this.remoteTwitterCachingEnabled = enabled;
      return this;
    }

    public Builder remoteTwitterCacheAddress(@Nullable String twitterCacheAddress) {
      this.remoteTwitterCacheAddress = twitterCacheAddress;
      return this;
    }

    public Builder googleGeolocationEnabled(boolean enabled) {
      this.googleGeolocationEnabled = enabled;
      return this;
    }

    public Builder yahooConsumerKey(String key) {
      this.yahooConsumerKey = key;
      return this;
    }

    public Builder yahooConsumerSecret(String secret) {
      this.yahooConsumerSecret = secret;
      return this;
    }

    public Builder yahooGeolocationEnabled(boolean enabled) {
      this.yahooGeolocationEnabled = enabled;
      return this;
    }

    public Builder tweetUpdateServletEnabled(boolean enabled) {
      this.tweetUpdateServletEnabled = enabled;
      return this;
    }

    public Builder systemNotificationList(List<String> systemNotificationList) {
      this.systemNotificationList = ImmutableList.copyOf(systemNotificationList);
      return this;
    }

    public Builder center(Location location) {
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
