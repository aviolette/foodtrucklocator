package foodtruck.time;

import java.time.ZonedDateTime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author aviolette
 * @since 11/10/18
 */
public class TimeConversionUtils {

  public static DateTime toJoda(ZonedDateTime ldt) {
    DateTimeZone zone = DateTimeZone.forID(ldt.getZone().getId());
    return new DateTime(ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute(), ldt.getSecond(), zone);
  }
}
