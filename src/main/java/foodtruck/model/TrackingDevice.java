package foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 7/28/16
 */
public class TrackingDevice extends ModelEntity {
  private String label;
  private String deviceNumber;
  private boolean enabled;
  // nullable means that it is not yet associated with a truck (i.e. not instrumented)
  private @Nullable String truckOwnerId;
  private @Nullable DateTime lastBroadcast;
  private @Nullable DateTime lastModified;
  private @Nullable Location lastLocation;
  private boolean parked;
  private boolean atBlacklistedLocation;

  private TrackingDevice(Builder builder) {
    super(builder.key);
    this.label = builder.label;
    this.deviceNumber = builder.deviceNumber;
    this.enabled = builder.enabled;
    this.truckOwnerId = builder.truckOwnerId;
    this.lastBroadcast = builder.lastBroadcast;
    this.lastModified = builder.lastModified;
    this.lastLocation = builder.lastLocation;
    this.parked = builder.parked;
    this.atBlacklistedLocation = builder.atBlacklistedLocation;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(@Nullable TrackingDevice device) {
    return (device == null) ?  new Builder() : new Builder(device);
  }

  public boolean isParked() {
    return parked;
  }

  @Nullable
  public String getTruckOwnerId() {
    return truckOwnerId;
  }

  @Nullable
  public DateTime getLastBroadcast() {
    return lastBroadcast;
  }

  @Nullable
  public DateTime getLastModified() {
    return lastModified;
  }

  @Nullable
  public Location getLastLocation() {
    return lastLocation;
  }

  public String getLabel() {
    return label;
  }

  public String getDeviceNumber() {
    return deviceNumber;
  }

  public boolean isAtBlacklistedLocation() {
    return atBlacklistedLocation;
  }

  public boolean isEnabled() {
    return enabled && !Strings.isNullOrEmpty(truckOwnerId);
  }

  public Long getId() {
    return (Long) getKey();
  }

  public static class Builder {
    private long key;
    private String label;
    private String deviceNumber;
    private boolean enabled;
    private @Nullable String truckOwnerId;
    private @Nullable DateTime lastBroadcast;
    private @Nullable DateTime lastModified;
    private @Nullable Location lastLocation;
    private boolean parked;
    private boolean atBlacklistedLocation;

    public Builder() {
    }

    public Builder(TrackingDevice device) {
      this.key = (Long)device.getKey();
      this.label = device.getLabel();
      this.deviceNumber = device.getDeviceNumber();
      this.enabled = device.isEnabled();
      this.truckOwnerId = device.truckOwnerId;
      this.lastModified = device.lastModified;
      this.lastBroadcast = device.lastBroadcast;
      this.lastLocation = device.lastLocation;
      this.parked = device.parked;
      this.atBlacklistedLocation = device.atBlacklistedLocation;
    }

    public Builder atBlacklistedLocation(boolean atBlacklistedLocation) {
      this.atBlacklistedLocation = atBlacklistedLocation;
      return this;
    }

    public Builder parked(boolean parked) {
      this.parked = parked;
      return this;
    }

    public Builder truckOwnerId(String truckOwnerId) {
      this.truckOwnerId = truckOwnerId;
      return this;
    }

    public Builder lastModified(DateTime dateTime) {
      this.lastModified = dateTime;
      return this;
    }

    public Builder lastBroadcast(DateTime dateTime) {
      this.lastBroadcast = dateTime;
      return this;
    }

    public Builder lastLocation(Location location) {
      this.lastLocation = location;
      return this;
    }

    public Builder key(long key) {
      this.key = key;
      return this;
    }

    public Builder label(String label) {
      this.label = label;
      return this;
    }

    public Builder deviceNumber(String deviceNumber) {
      this.deviceNumber = deviceNumber;
      return this;
    }

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public TrackingDevice build() {
      return new TrackingDevice(this);
    }
  }
}
