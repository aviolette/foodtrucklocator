package net.andrewviolette.foodtruck.model;

import com.google.common.base.Objects;

import org.joda.time.DateTime;


/**
 * Specifies an truck at a location at a date and time.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class TruckStop {
  private final Truck truck;
  private final DateTime startTime;
  private final DateTime endTime;
  private final Location location;

  public TruckStop(Truck truck, DateTime startTime, DateTime endTime, Location location) {
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
    return truck.equals(obj.truck) && startTime.equals(obj.startTime) && endTime.equals(obj.endTime) &&
        location.equals(obj.location);
  }
}
