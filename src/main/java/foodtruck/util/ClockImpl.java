package foodtruck.util;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.model.DayOfWeek;

/**
 * @author aviolette@gmail.com
 * @since 9/22/11
 */
public class ClockImpl implements Clock {
  private final DateTimeZone zone;

  @Inject
  public ClockImpl(DateTimeZone zone) {
    this.zone = zone;
  }

  @Override
  public DateTime now() {
    return new DateTime(zone);
  }

  @Override
  public LocalDate currentDay() {
    return new LocalDate(zone);
  }

  @Override public DayOfWeek dayOfWeek() {
    switch (currentDay().getDayOfWeek()) {
      case DateTimeConstants.MONDAY:
        return DayOfWeek.monday;
      case DateTimeConstants.TUESDAY:
        return DayOfWeek.tuesday;
      case DateTimeConstants.WEDNESDAY:
        return DayOfWeek.wednesday;
      case DateTimeConstants.THURSDAY:
        return DayOfWeek.thursday;
      case DateTimeConstants.FRIDAY:
        return DayOfWeek.friday;
      case DateTimeConstants.SATURDAY:
        return DayOfWeek.saturday;
    }
    return DayOfWeek.sunday;
  }

  @Override public LocalDate firstDayOfWeek() {
    return firstDayOfWeekFrom(now());
  }

  public LocalDate firstDayOfWeekFrom(DateTime dt) {
    if (dt.getDayOfWeek() == DateTimeConstants.SUNDAY) {
      return dt.toLocalDate();
    }
    return dt.minusDays(now().getDayOfWeek()).toLocalDate();
  }

}
