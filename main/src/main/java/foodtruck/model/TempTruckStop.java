package foodtruck.model;

import java.time.ZonedDateTime;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * @author aviolette
 * @since 2018-12-09
 */
public class TempTruckStop extends ModelEntity {
  private final String truckId;
  private final ZonedDateTime startTime;
  private final ZonedDateTime endTime;
  private final String locationName;
  private final String calendarName;

  private TempTruckStop(Builder builder) {
    this.truckId = builder.truckId;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.locationName = builder.locationName;
    this.key = builder.key;
    this.calendarName = builder.calendarName;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getTruckId() {
    return truckId;
  }

  public ZonedDateTime getStartTime() {
    return startTime;
  }

  public ZonedDateTime getEndTime() {
    return endTime;
  }

  public String getLocationName() {
    return locationName;
  }

  public String getCalendarName() {
    return calendarName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TempTruckStop that = (TempTruckStop) o;
    return Objects.equal(truckId, that.truckId) && Objects.equal(startTime, that.startTime) &&
        Objects.equal(endTime, that.endTime) && Objects.equal(locationName, that.locationName) &&
        Objects.equal(calendarName, that.calendarName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(truckId, startTime, endTime, locationName, calendarName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("truckId", truckId)
        .add("startTime", startTime)
        .add("endTime", endTime)
        .add("location", locationName)
        .add("calendar", calendarName)
        .toString();
  }

  public static class Builder {
    private String truckId;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private String locationName;
    private long key = -1;
    private String calendarName;

    public Builder calendarName(String name) {
      this.calendarName = name;
      return this;
    }

    public Builder truckId(String truckId) {
      this.truckId = truckId;
      return this;
    }
    
    public Builder startTime(ZonedDateTime startTime) {
      this.startTime = startTime;
      return this;
    }

    public Builder endTime(ZonedDateTime endTime) {
      this.endTime = endTime;
      return this;
    }

    public Builder locationName(String locationName) {
      this.locationName = locationName;
      return this;
    }

    public Builder key(long key) {
      this.key = key;
      return this;
    }

    public TempTruckStop build() {
      return new TempTruckStop(this);
    }
  }
}
