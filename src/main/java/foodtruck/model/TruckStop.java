package foodtruck.model;

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

  public TruckStop(Truck truck, DateTime startTime, DateTime endTime, Location location,
      @Nullable Object key) {
    super(key);
    this.truck = truck;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
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
    return Objects.toStringHelper(this).add("truck", truck.getId()).add("startTime", startTime)
        .add("endTime", endTime).add("location", location).toString();
  }

  /**
   * Returns a new TruckStop with a new startTime
   */
  public TruckStop withStartTime(DateTime startTime) {
    return new TruckStop(truck, startTime, endTime, location, getKey());
  }

  /**
   * Returns a new TruckStop with a new endTime
   */
  public TruckStop withEndTime(DateTime endTime) {
    return new TruckStop(truck, startTime, endTime, location, getKey());
  }

  public boolean activeDuring(DateTime dateTime) {
    return startTime.equals(dateTime) || (dateTime.isAfter(startTime) && dateTime.isBefore(endTime));
  }
}
