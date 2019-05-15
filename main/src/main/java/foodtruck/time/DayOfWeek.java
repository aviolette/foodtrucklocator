package foodtruck.time;

import javax.annotation.Nullable;

import org.joda.time.DateTimeConstants;

/**
 * An enum for each day of the week.
 * @author aviolette@gmail.com
 * @since Jul 14, 2011
 */
public enum DayOfWeek {
  monday(DateTimeConstants.MONDAY, "mon|monday"),
  tuesday(DateTimeConstants.TUESDAY, "tue|tu|tues|tuesday"),
  wednesday(DateTimeConstants.WEDNESDAY, "wed|weds|wednesday"),
  thursday(DateTimeConstants.THURSDAY, "thu|th|thursday|thurs"),
  friday(DateTimeConstants.FRIDAY, "fri|friday"),
  saturday(DateTimeConstants.SATURDAY, "sat|saturday"),
  sunday(DateTimeConstants.SUNDAY, "sun|su|sunday");

  private final String matchPattern;
  private int isoConstant;

  DayOfWeek(int dateConstant, String matchPattern) {
    isoConstant = dateConstant;
    this.matchPattern = matchPattern;
  }

  public static @Nullable DayOfWeek fromConstant(int dayOfWeek) {
    DayOfWeek[] values = values();
    if (dayOfWeek >= values.length+1) {
      return null;
    }
    return values[dayOfWeek-1];
  }

  public boolean isWeekend() {
    return isoConstant == DateTimeConstants.SATURDAY ||
        isoConstant == DateTimeConstants.SUNDAY;
  }

  public int getConstant() {
    return isoConstant;
  }

  public String getMatchPattern() {
    return matchPattern;
  }
}
