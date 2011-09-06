package foodtruck.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

import org.joda.time.DateTime;


/**
 * Specifies an truck at a location at a date and time.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
@XmlRootElement
public class TruckStop {
  private Truck truck;
  private DateTime startTime;
  private DateTime endTime;
  private Location location;

  // for JAXB
  public TruckStop() {
  }

  public TruckStop(Truck truck, DateTime startTime, DateTime endTime, Location location) {
    this.truck = truck;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
  }

  @XmlElement
  public Truck getTruck() {
    return truck;
  }

  @XmlElement
  public DateTime getStartTime() {
    return startTime;
  }

  @XmlElement
  public DateTime getEndTime() {
    return endTime;
  }

  @XmlElement
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
}
