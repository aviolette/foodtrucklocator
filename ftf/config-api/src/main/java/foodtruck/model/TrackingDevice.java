package foodtruck.model;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static foodtruck.model.TrackingDeviceState.BROADCASTING;
import static foodtruck.model.TrackingDeviceState.HIDDEN;
import static foodtruck.model.TrackingDeviceState.MOVING;

/**
 * Represents the last recorded state of a tracking device that is in a truck.
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
  private @Nullable String fuelLevel;
  private @Nullable String batteryCharge;
  private int degreesFromNorth;
  private @Nullable Location lastActualLocation;

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
    this.fuelLevel = builder.fuelLevel;
    this.batteryCharge = builder.batteryCharge;
    this.batteryCharge = builder.batteryCharge;
    this.degreesFromNorth = builder.degreesFromNorth;
    this.lastActualLocation = builder.lastActualLocation;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(@Nullable TrackingDevice device) {
    return (device == null) ? new Builder() : new Builder(device);
  }

  @Nullable
  public Location getLastActualLocation() {
    return lastActualLocation;
  }

  public boolean isHasWarning() {
    return !Strings.isNullOrEmpty(getWarning());
  }

  public TrackingDeviceState getState() {
    if (enabled) {
      if (!parked) {
        return MOVING;
      }
      if (atBlacklistedLocation) {
        return HIDDEN;
      }
      return BROADCASTING;
    }
    return HIDDEN;
  }

  @Nullable
  public String getBatteryCharge() {
    return batteryCharge;
  }

  public int getBatteryChargeValue() {
    if (Strings.isNullOrEmpty(batteryCharge)) {
      return 0;
    }
    float f = Float.parseFloat(batteryCharge);
    return (int) ((f / 15f) * 100f);
  }

  @Nullable
  public String getFuelLevel() {
    return fuelLevel;
  }

  public int getFuelLevelValue() {
    if (Strings.isNullOrEmpty(fuelLevel)) {
      return 0;
    }
    String fuel = fuelLevel.replace("%", "");
    int index = fuel.indexOf('.');
    if (index != -1) {
      fuel = fuel.substring(0, index);
    }
    return Ints.tryParse(fuel);
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("label", label)
        .add("device number", deviceNumber)
        .add("enabled", enabled)
        .add("truck owner", truckOwnerId)
        .add("last broadcast", lastBroadcast)
        .add("last modified", lastModified)
        .add("last location", lastLocation)
        .add("parked", parked)
        .add("fuel level", fuelLevel)
        .add("battery charge", batteryCharge)
        .add("at blacklisted location", atBlacklistedLocation)
        .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, label, deviceNumber, enabled, truckOwnerId, lastBroadcast, lastModified, lastLocation,
        parked, fuelLevel, batteryCharge, atBlacklistedLocation, lastActualLocation);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof TrackingDevice)) {
      return false;
    }
    TrackingDevice td = (TrackingDevice) obj;
    return label.equals(td.label) && deviceNumber.equals(td.deviceNumber) && enabled == td.enabled && Objects.equals(
        truckOwnerId, td.truckOwnerId) &&
        Objects.equals(key, td.key) &&
        Objects.equals(lastActualLocation, td.lastActualLocation) &&
        Objects.equals(lastBroadcast, td.lastBroadcast) && Objects.equals(lastModified,
        td.lastModified) && Objects.equals(lastLocation, td.lastLocation) &&
        parked == td.parked && atBlacklistedLocation == td.atBlacklistedLocation && Objects.equals(fuelLevel,
        td.fuelLevel) && Objects.equals(batteryCharge, td.batteryCharge) && degreesFromNorth == td.degreesFromNorth;
  }

  public int getDegreesFromNorth() {
    return degreesFromNorth;
  }

  @Nullable
  public Location getPreciseLocation() {
    return MoreObjects.firstNonNull(getLastActualLocation(), getLastLocation());
  }

  @Nullable
  public String getWarning() {
    if (lastBroadcast == null) {
      return "Device has never broadcasted";
    }
    if (lastBroadcast.isAfter(new DateTime().minusDays(1))) {
      return null;
    }
    DateTimeFormatter formatter = DateTimeFormat.forStyle("M-");

    return "Device hasn't broadcast since " + formatter.print(lastBroadcast) + " . Is it connected properly?";
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
    private @Nullable String fuelLevel;
    private @Nullable String batteryCharge;
    private int degreesFromNorth;
    private @Nullable Location lastActualLocation;

    public Builder() {
    }

    public Builder(TrackingDevice device) {
      this.key = (Long) device.getKey();
      this.label = device.getLabel();
      this.deviceNumber = device.getDeviceNumber();
      this.enabled = device.isEnabled();
      this.truckOwnerId = device.truckOwnerId;
      this.lastModified = device.lastModified;
      this.lastBroadcast = device.lastBroadcast;
      this.lastLocation = device.lastLocation;
      this.parked = device.parked;
      this.atBlacklistedLocation = device.atBlacklistedLocation;
      this.degreesFromNorth = device.degreesFromNorth;
      this.lastActualLocation = device.lastActualLocation;
    }

    public Builder degreesFromNorth(int degreesFromNorth) {
      this.degreesFromNorth = degreesFromNorth;
      return this;
    }

    public Builder fuelLevel(String fuelLevel) {
      this.fuelLevel = fuelLevel;
      return this;
    }

    public Builder batteryCharge(String batteryCharge) {
      this.batteryCharge = batteryCharge;
      return this;
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

    public Builder lastActualLocation(Location actualLocation) {
      this.lastActualLocation = actualLocation;
      return this;
    }
  }
}
