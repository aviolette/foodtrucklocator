package foodtruck.time;

import java.time.ZonedDateTime;

import org.joda.time.Interval;
import org.joda.time.Period;

import static foodtruck.time.TimeConversionUtils.toJoda;

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



  public static boolean overlap(ZonedDateTime startTime, ZonedDateTime endTime, ZonedDateTime startTime1,
      ZonedDateTime endTime1) {
    Interval interval1 = new Interval(toJoda(startTime), toJoda(endTime));
    Interval interval2 = new Interval(toJoda(startTime1), toJoda(endTime1));
    return interval1.overlaps(interval2);
  }


  public static boolean overlapsOrContains(ZonedDateTime startTime, ZonedDateTime endTime, ZonedDateTime startTime1,
      ZonedDateTime endTime1) {
    return overlap(startTime, endTime, startTime1, endTime1) || overlap(startTime1, endTime1, startTime, endTime);
  }

}
