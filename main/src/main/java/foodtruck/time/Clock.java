package foodtruck.time;

import java.time.ZonedDateTime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

/**
 * A clock that returns the current time for the current zone.
 * @author aviolette@gmail.com
 * @since 9/22/11
 */
public interface Clock {
  /**
   * Returns the date time for the current instant.
   */
  DateTime now();

  /**
   * Returns a java 8 zoned date time for the current instant.
   * @return
   */
  ZonedDateTime now8();

  /**
   * Returns the current day in the current time zone.
   */
  LocalDate currentDay();

  /**
   * Returns the current day of the week
   */
  DayOfWeek dayOfWeek();

  /**
   * Returns the first day of this week.
   */
  LocalDate firstDayOfWeek();

  /**
   * Returns the first day of the week that the specified time falls within.
   * @param dt a time within a week.
   * @return the first day of the week.
   */
  LocalDate firstDayOfWeekFrom(DateTime dt);

  /**
   * Returns the active zone
   * @return the time zone
   */
  DateTimeZone zone();

  /**
   * Returns the current time/date formatted as a string
   */
  String nowFormattedAsTime();

  /**
   * Returns a time and the specified hour and minute on the current day
   */
  DateTime timeAt(int hour, int minute);
}
