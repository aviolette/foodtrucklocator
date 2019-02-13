package foodtruck.time;

import org.joda.time.Period;

/**
 * @author aviolette
 * @since 2019-02-13
 */
public class TimeUtils {

  public static String period(Period p) {
    String period = p.getHours() + " hours " + p.getMinutes() + " minutes";

    if (p.getDays() > 0) {
      period = p.getDays() + " days " + period;
    }
    if (p.getWeeks() > 0) {
      period = p.getWeeks() + " weeks " + period;
    }
    if (p.getMonths() > 0) {
      period = p.getMonths() + " months " + period;
    }
    return period;
  }
}
