package foodtruck.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

import org.joda.time.DateTime;


/**
 * Specifies an truck at a location at a date and time.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class TruckStop extends ModelEntity {
  private final Truck truck;
  private final DateTime startTime;
  private final DateTime endTime;
  private final Location location;
  private final boolean locked;
  private final @Nullable DateTime fromBeacon;
  private static final Logger log = Logger.getLogger(TruckStop.class.getName());
  private TruckStop(Builder builder) {
    super(builder.key);
    truck = builder.truck;
    startTime = builder.startTime;
    endTime = builder.endTime;
    location = builder.location;
    locked = builder.locked;
    fromBeacon = builder.fromBeacon;
  }

  @Deprecated // use builder instead
  public TruckStop(Truck truck, DateTime startTime, DateTime endTime, Location location,
      @Nullable Long key, boolean locked) {
    super(key);
    this.truck = truck;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
    this.locked = locked;
    this.fromBeacon = null;
  }

  public boolean isLocked() {
    return locked;
  }

  public Truck getTruck() {
    return truck;
  }

  public DateTime getStartTime() {
    return startTime;
  }

  public DateTime getEndTime() {
    return endTime;
  }

  public Location getLocation() {
    return location;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(truck, startTime, endTime, location);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null) {
      return false;
    } else if (!(o instanceof TruckStop)) {
      return false;
    }
    TruckStop obj = (TruckStop) o;
    return truck.equals(obj.truck) && startTime.equals(obj.startTime) &&
        endTime.equals(obj.endTime) &&
        location.equals(obj.location);
  }

  /**
   * Returns true if the start time of the stop falls within the specified time range.
   */
  public boolean within(TimeRange range) {
    return range.getStartDateTime().isBefore(startTime.plusSeconds(1)) &&
        range.getEndDateTime().isAfter(startTime);
  }

  @Override
  public String toString() {
//    return "";
    try {
      return Objects.toStringHelper(this).add("truck", truck.getId()).add("startTime", startTime)
          .add("endTime", endTime).add("location", location).add("locked", locked).toString();
    } catch (Throwable t) {
      log.log(Level.WARNING, t.getMessage(), t);
      return truck.getId();
    }
  }

  /**
   * Returns a new TruckStop with a new startTime
   */
  public TruckStop withStartTime(DateTime startTime) {
    return new TruckStop(truck, startTime, endTime, location, (Long) getKey(), locked);
  }

  /**
   * Returns a new TruckStop with a new endTime
   */
  public TruckStop withEndTime(DateTime endTime) {
    return new TruckStop(truck, startTime, endTime, location, (Long) getKey(), locked);
  }

  public boolean activeDuring(DateTime dateTime) {
    return startTime.equals(dateTime) ||
        (dateTime.isAfter(startTime) && dateTime.isBefore(endTime));
  }

  public TruckStop withLocation(Location location) {
    return new TruckStop(truck, startTime, endTime, location, (Long) getKey(), locked);
  }

  /**
   * Returns true if the stop has expired by the specified time.
   */
  public boolean hasExpiredBy(DateTime currentTime) {
    return endTime.isBefore(currentTime);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(TruckStop stop) {
    return new Builder(stop);
  }

  public @Nullable DateTime getBeaconTime() {
    return fromBeacon;
  }

  public boolean isFromBeacon() {
    return fromBeacon != null;
  }

  public static class Builder {
    private Truck truck;
    private DateTime startTime;
    private DateTime endTime;
    private Location location;
    private boolean locked;
    private @Nullable DateTime fromBeacon;
    private @Nullable Long key;

    private Builder() {}

    private Builder(TruckStop stop) {
      truck = stop.getTruck();
      startTime = stop.getStartTime();
      endTime = stop.getEndTime();
      location = stop.getLocation();
      locked = stop.isLocked();
      fromBeacon = stop.getBeaconTime();
      key = (Long) stop.getKey();
    }

    public Builder key(@Nullable Long key) {
      this.key = key;
      return this;
    }

    public Builder truck(Truck truck) {
      this.truck = truck;
      return this;
    }

    public Builder startTime(DateTime dateTime) {
      this.startTime = dateTime;
      return this;
    }

    public Builder endTime(DateTime endTime) {
      this.endTime = endTime;
      return this;
    }

    public Builder location(Location location) {
      this.location = location;
      return this;
    }

    public Builder locked(boolean locked) {
      this.locked = locked;
      return this;
    }

    public Builder fromBeacon(@Nullable DateTime fromBeacon) {
      this.fromBeacon = fromBeacon;
      return this;
    }

    public TruckStop build() {
      return new TruckStop(this);
    }
  }
}
