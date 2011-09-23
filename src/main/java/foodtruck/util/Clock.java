package foodtruck.util;

import org.joda.time.DateTime;
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
  public DateTime now();

  /**
   * Returns the current day in the current time zone.
   */
  LocalDate currentDay();
}
