package foodtruck.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.model.DayOfWeek;

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
   * Returns the current day in the current time zone.
   */
  LocalDate currentDay();

  /**
   * Returns the current day of the week
   */
  DayOfWeek dayOfWeek();

  /**
   * Returns the first day of this week.
   * @return
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
   * Returns the current time formatted as a string (w/o a date)
   * @return
   */
  String nowFormattedAsTime();

  /**
   * Returns a time and the specified hour and minute on the current day
   */
  DateTime timeAt(int hour, int minute);
}
