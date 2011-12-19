package foodtruck.model;

import org.joda.time.DateTimeConstants;

/**
 * An enum for each day of the week.
 * @author aviolette@gmail.com
 * @since Jul 14, 2011
 */
public enum DayOfWeek {
  monday(DateTimeConstants.MONDAY), tuesday(DateTimeConstants.TUESDAY), wednesday(
      DateTimeConstants.WEDNESDAY),
  thursday(DateTimeConstants.THURSDAY), friday(DateTimeConstants.FRIDAY), saturday(
      DateTimeConstants.SATURDAY),
  sunday(DateTimeConstants.SUNDAY);

  private int isoConstant;

  DayOfWeek(int dateConstant) {
    isoConstant = dateConstant;
  }

  public int getConstant() {
    return isoConstant;
  }
}
