package foodtruck.model;

import com.google.common.base.Objects;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * A time range on a day.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class TimeRange {
  private static final LocalTime MIDNIGHT = new LocalTime(0, 0);
  private static final LocalTime JUST_BEFORE_MIDNIGHT = new LocalTime(23,59);
  private final LocalTime endTime;
  private final LocalTime startTime;
  private final LocalDate date;
  private final DateTimeZone zone;

  @Deprecated
  public TimeRange(LocalDate instant) {
    this(instant, DateTimeZone.UTC);
  }

  public TimeRange(LocalDate date, LocalTime startTime, LocalTime endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.date = date;
    this.zone = DateTimeZone.UTC;
  }

  public TimeRange(LocalDate instant, DateTimeZone zone) {
    date = instant;
    startTime = MIDNIGHT;
    endTime = JUST_BEFORE_MIDNIGHT;
    this.zone = zone;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(startTime, endTime, date);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null || !(o instanceof TimeRange)) {
      return false;
    }
    TimeRange obj = (TimeRange) o;
    return startTime.equals(obj.startTime) && endTime.equals(obj.endTime) && date.equals(obj.date);
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalDate getDate() {
    return date;
  }

  public DateTime getStartDateTime() {
    return date.toDateTime(startTime, zone);
  }

  public DateTime getEndDateTime() {
    return date.toDateTime(endTime, zone);
  }
}
