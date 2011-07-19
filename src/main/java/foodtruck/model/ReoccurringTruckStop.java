package foodtruck.model;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * Specifies a truck that is always at a location at a specific day of week and time range.
 * @author aviolette@gmail.com
 * @since Jul 14, 2011
 */
public class ReoccurringTruckStop {
  private final Truck truck;
  private final DayOfWeek day;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final Location location;

  public ReoccurringTruckStop(Truck truck, DayOfWeek dayOfWeek, LocalTime startTime,
      LocalTime endTime, Location location) {
    this.truck = truck;
    this.day = dayOfWeek;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
  }

  public boolean in(TimeRange range) {
    return range.getDate().getDayOfWeek() == day.getConstant() &&
        (startTime.isEqual(range.getStartTime()) || startTime.isAfter(range.getStartTime())) &&
        (endTime.isEqual(range.getEndTime()) || endTime.isBefore(range.getEndTime()));
  }

  public TruckStop toTruckStop(LocalDate localDate) {
    return new TruckStop(truck, localDate.toDateTime(startTime), localDate.toDateTime(endTime), location);
  }
}
