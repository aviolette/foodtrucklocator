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
  private DateTime throttleGoogleUntil;
  private @Nullable Location center;
  private List<String> systemNotificationList;
  private @Nullable String notificationSender;
  private @Nullable String frontDoorAppKey;
  private Confidence minimumConfidenceForDisplay;
  private String syncUrl;
  private String syncAppKey;

  public Configuration(Object key) {
    super(key);
  }

  private Configuration(Builder builder) {
    this(builder.key);
    this.throttleGoogleUntil = builder.throttleGoogleUntil;
    this.center = builder.center;
    this.systemNotificationList = builder.systemNotificationList;
    this.notificationSender = builder.notificationSender;
    this.frontDoorAppKey = builder.frontDoorAppKey;
    this.minimumConfidenceForDisplay = builder.minimumConfidenceForDisplay;
    this.syncUrl = builder.syncUrl;
    this.syncAppKey = builder.syncAppKey;
  }

  public Confidence getMinimumConfidenceForDisplay() {
    return minimumConfidenceForDisplay;
  }

  public String getDisplayConfidenceMinimum() {
    return minimumConfidenceForDisplay.toString();
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

  public String getSyncUrl() {
    return syncUrl;
  }

  public String getSyncAppKey() {
    return syncAppKey;
  }

  public static class Builder {
    private Key key;
    private @Nullable DateTime throttleGoogleUntil;
    private Location center;
    private List<String> systemNotificationList = ImmutableList.of();
    private @Nullable String notificationSender;
    private @Nullable String frontDoorAppKey;
    private Confidence minimumConfidenceForDisplay = Confidence.HIGH;
    private String syncAppKey;
    private String syncUrl;

    public Builder() {
    }

    public Builder(Configuration config) {
      this.syncUrl = config.syncUrl;
      this.syncAppKey = config.syncAppKey;
      this.throttleGoogleUntil = config.throttleGoogleUntil;
      this.key = (Key) config.getKey();
      this.systemNotificationList = config.systemNotificationList;
      this.notificationSender = config.notificationSender;
      this.frontDoorAppKey = config.frontDoorAppKey;
    }

    public Builder syncAppKey(String syncAppKey) {
      this.syncAppKey = syncAppKey;
      return this;
    }

    public Builder syncUrl(String syncUrl) {
      this.syncUrl = syncUrl;
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

    public Builder minimumConfidenceForDisplay(Confidence confidence) {
      this.minimumConfidenceForDisplay = confidence;
      return this;
    }
  }
}
