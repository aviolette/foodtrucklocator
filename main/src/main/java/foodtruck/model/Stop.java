package foodtruck.model;

import com.google.common.base.MoreObjects;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 11/2/16
 */
public class Stop {
  private String id;
  private String deviceId;
  private String driverName;
  private Location location;
  private String stopType;
  private Duration duration;
  private DateTime beginDate;
  private DateTime endDate;

  private Stop(Builder builder) {
    this.id = builder.id;
    this.deviceId = builder.deviceId;
    this.driverName = builder.driverName;
    this.location = builder.location;
    this.stopType = builder.stopType;
    this.duration = builder.duration;
    this.beginDate = builder.beginDate;
    this.endDate = builder.endDate;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Stop instance) {
    return new Builder(instance);
  }

  public Duration getDuration() {
    return duration;
  }

  public String getId() {
    return id;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getDriverName() {
    return driverName;
  }

  public Location getLocation() {
    return location;
  }

  public String getStopType() {
    return stopType;
  }

  public DateTime getBeginTime() {
    return beginDate;
  }

  public DateTime getEndTime() {
    return endDate;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("deviceId", deviceId)
        .add("driverName", driverName)
        .add("location", location)
        .add("stopType", stopType)
        .add("beginDate", beginDate)
        .add("endDate", endDate)
        .add("duration", duration)
        .toString();
  }

  public static class Builder {
    private String id;
    private String deviceId;
    private String driverName;
    private Location location;
    private String stopType;
    private Duration duration;
    private DateTime beginDate;
    private DateTime endDate;

    public Builder() {
    }

    public Builder(Stop instance) {
      this.id = instance.id;
      this.deviceId = instance.deviceId;
      this.driverName = instance.driverName;
      this.location = instance.location;
      this.stopType = instance.stopType;
      this.duration = instance.duration;
      this.beginDate = instance.beginDate;
      this.endDate = instance.endDate;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder endDate(DateTime endDate) {
      this.endDate = endDate;
      return this;
    }

    public Builder beginDate(DateTime dateTime) {
      this.beginDate = dateTime;
      return this;
    }

    public Builder duration(Duration duration) {
      this.duration = duration;
      return this;
    }

    public Builder deviceId(String deviceId) {
      this.deviceId = deviceId;
      return this;
    }

    public Builder driverName(String driverName) {
      this.driverName = driverName;
      return this;
    }

    public Builder location(Location location) {
      this.location = location;
      return this;
    }

    public Builder stopType(String stopType) {
      this.stopType = stopType;
      return this;
    }

    public Stop build() {
      return new Stop(this);
    }
  }
}
