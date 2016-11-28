package foodtruck.linxup;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.javadocmd.simplelatlng.LatLng;

import org.joda.time.DateTime;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 7/24/16
 */
@SuppressWarnings("WeakerAccess")
public class Position {
  private final DateTime date;
  private final String vehicleLabel;
  private final LatLng latLng;
  private final int direction;
  private final int speedMph;
  private final boolean speeding;
  private final int estimatedSpeedLimit;
  private final @Nullable BehaviorCode behaviorCode;
  private final String deviceNumber;
  private final String driverId;
  private final String simDeviceNumber;
  private final String deviceTypeDescription;
  private final String fuelLevel;
  private final String batteryCharge;

  private Position(Builder builder) {
    this.date = builder.date;
    this.vehicleLabel = builder.vehicleLabel;
    this.latLng = builder.latLng;
    this.direction = builder.direction;
    this.speedMph = builder.speedMph;
    this.speeding = builder.speeding;
    this.estimatedSpeedLimit = builder.estimatedSpeedLimit;
    this.behaviorCode = builder.behaviorCode;
    this.deviceNumber = builder.deviceNumber;
    this.driverId = builder.driverId;
    this.simDeviceNumber = builder.simDeviceNumber;
    this.deviceTypeDescription = builder.deviceTypeDescription;
    this.fuelLevel = builder.fuelLevel;
    this.batteryCharge = builder.batteryCharge;

  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Position position) {
    return new Builder(position);
  }

  public DateTime getDate() {
    return date;
  }

  public String getVehicleLabel() {
    return vehicleLabel;
  }

  public LatLng getLatLng() {
    return latLng;
  }

  public int getDirection() {
    return direction;
  }

  public int getSpeedMph() {
    return speedMph;
  }

  public boolean isSpeeding() {
    return speeding;
  }

  public int getEstimatedSpeedLimit() {
    return estimatedSpeedLimit;
  }

  @Nullable
  public BehaviorCode getBehaviorCode() {
    return behaviorCode;
  }

  public boolean isParked() {
    return speedMph == 0;
  }

  public Location toLocation() {
    return Location.builder()
        .lat(getLatLng().getLatitude())
        .lng(getLatLng().getLongitude())
        .build();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("Date", date)
        .add("Vehicle Label", vehicleLabel)
        .add("Lat / Lng", latLng)
        .add("Direction", direction)
        .add("Speed (in mph)", speedMph)
        .add("Speeding", speeding)
        .add("Estimated speed limit", estimatedSpeedLimit)
        .add("Device ID", deviceNumber)
        .add("Device Type", deviceTypeDescription)
        .toString();
  }

  public String getDeviceNumber() {
    return deviceNumber;
  }

  public String getFuelLevel() {
    return fuelLevel;
  }

  public String getBatteryCharge() {
    return batteryCharge;
  }

  public static class Builder {
    private DateTime date;
    private String vehicleLabel;
    private LatLng latLng;
    private int direction;
    private int speedMph;
    private boolean speeding;
    private int estimatedSpeedLimit;
    private @Nullable BehaviorCode behaviorCode;
    private String deviceNumber;
    private String driverId;
    private String simDeviceNumber;
    private String deviceTypeDescription;
    private String fuelLevel;
    private String batteryCharge;

    public Builder() {
    }

    public Builder(Position position) {
      this.date = position.date;
      this.vehicleLabel = position.vehicleLabel;
      this.latLng = position.latLng;
      this.direction = position.direction;
      this.speedMph = position.speedMph;
      this.speeding = position.speeding;
      this.estimatedSpeedLimit = position.estimatedSpeedLimit;
      this.deviceNumber = position.deviceNumber;
      this.driverId = position.driverId;
      this.simDeviceNumber = position.simDeviceNumber;
      this.deviceTypeDescription = position.deviceTypeDescription;
      this.fuelLevel = position.fuelLevel;
      this.batteryCharge = position.batteryCharge;
    }

    public Builder date(DateTime date) {
      this.date = date;
      return this;
    }

    Builder fuelLevel(String fuelLevel) {
      this.fuelLevel = fuelLevel;
      return this;
    }

    Builder batteryCharge(String batteryCharge) {
      this.batteryCharge = batteryCharge;
      return this;
    }

    Builder deviceNumber(String deviceNumber) {
      this.deviceNumber = deviceNumber;
      return this;
    }

    Builder driverId(String driverId) {
      this.driverId = driverId;
      return this;
    }

    Builder simDeviceNumber(String deviceNumber) {
      this.simDeviceNumber = deviceNumber;
      return this;
    }

    Builder deviceTypeDescription(String deviceTypeDescription) {
      this.deviceTypeDescription = deviceTypeDescription;
      return this;
    }

    Builder speeding(boolean speeding) {
      this.speeding = speeding;
      return this;
    }

    Builder vehicleLabel(String vehicleLabel) {
      this.vehicleLabel = vehicleLabel;
      return this;
    }

    Builder latLng(LatLng position) {
      this.latLng = position;
      return this;
    }

    Builder direction(int direction) {
      this.direction = direction;
      return this;
    }

    Builder speedMph(int speedMph) {
      this.speedMph = speedMph;
      return this;
    }

    Builder estimatedSpeedLimit(int estimatedSpeedLimit) {
      this.estimatedSpeedLimit = estimatedSpeedLimit;
      return this;
    }

    Builder behaviorCode(BehaviorCode behaviorCode) {
      this.behaviorCode = behaviorCode;
      return this;
    }

    public Position build() {
      return new Position(this);
    }
  }
}
