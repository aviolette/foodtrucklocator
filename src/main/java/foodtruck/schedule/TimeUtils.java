package foodtruck.schedule;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author aviolette@gmail.com
 * @since 8/29/11
 */
public class TimeUtils {
  public static DateTime toJoda(com.google.gdata.data.DateTime gdataTime, DateTimeZone zone) {
    return new DateTime(gdataTime.getValue(), zone);
  }
}
