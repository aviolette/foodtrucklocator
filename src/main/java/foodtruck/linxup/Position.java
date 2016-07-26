package foodtruck.linxup;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.javadocmd.simplelatlng.LatLng;

import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 7/24/16
 */
public class Position {
  private final DateTime date;
  private final String vehicleLabel;
  private final LatLng latLng;
  private final long altitude;
  private final int direction;
  private final int speedMph;
  private final boolean speeding;
  private final int estimatedSpeedLimit;
  private final @Nullable BehaviorCode behaviorCode;

  private Position(Builder builder) {
    this.date = builder.date;
    this.vehicleLabel = builder.vehicleLabel;
    this.latLng = builder.latLng;
    this.altitude = builder.altitude;
    this.direction = builder.direction;
    this.speedMph = builder.speedMph;
    this.speeding = builder.speeding;
    this.estimatedSpeedLimit = builder.estimatedSpeedLimit;
    this.behaviorCode = builder.behaviorCode;
  }

  public static Builder builder() {
    return new Builder();
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

  public long getAltitude() {
    return altitude;
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

  public @Nullable BehaviorCode getBehaviorCode() {
    return behaviorCode;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("Date", date)
        .add("Vehicle Label", vehicleLabel)
        .add("Lat / Lng", latLng)
        .add("Altitude", altitude)
        .add("Direction", direction)
        .add("Speed (in mph)", speedMph)
        .add("Speeding", speeding)
        .add("Estimated speed limit", estimatedSpeedLimit)
        .toString();
  }

  public static class Builder {
    private DateTime date;
    private String vehicleLabel;
    private LatLng latLng;
    private long altitude;
    private int direction;
    private int speedMph;
    private boolean speeding;
    private int estimatedSpeedLimit;
    private @Nullable BehaviorCode behaviorCode;

    public Builder() {}

    public Builder(Position position) {
      this.date = position.date;
      this.vehicleLabel = position.vehicleLabel;
      this.latLng = position.latLng;
      this.altitude = position.altitude;
      this.direction = position.direction;
      this.speedMph = position.speedMph;
      this.speeding = position.speeding;
      this.estimatedSpeedLimit = position.estimatedSpeedLimit;
    }

    public Builder date(DateTime date) {
      this.date = date;
      return this;
    }

    public Builder speeding(boolean speeding) {
      this.speeding = speeding;
      return this;
    }

    public Builder vehicleLabel(String vehicleLabel) {
      this.vehicleLabel = vehicleLabel;
      return this;
    }

    public Builder latLng(LatLng position) {
      this.latLng = position;
      return this;
    }

    public Builder altitude(long altitude) {
      this.altitude = altitude;
      return this;
    }

    public Builder direction(int direction) {
      this.direction = direction;
      return this;
    }

    public Builder speedMph(int speedMph) {
      this.speedMph = speedMph;
      return this;
    }

    public Builder estimatedSpeedLimit(int estimatedSpeedLimit) {
      this.estimatedSpeedLimit = estimatedSpeedLimit;
      return this;
    }

    public Builder behaviorCode(BehaviorCode behaviorCode) {
      this.behaviorCode = behaviorCode;
      return this;
    }

    public Position build() {
      return new Position(this);
    }
  }
}
