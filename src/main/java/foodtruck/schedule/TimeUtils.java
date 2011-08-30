package foodtruck.schedule;

import org.joda.time.DateTime;

/**
 * @author aviolette@gmail.com
 * @since 8/29/11
 */
public class TimeUtils {
  public static DateTime toJoda(com.google.gdata.data.DateTime gdataTime) {
    return new DateTime(gdataTime.getValue());
  }
}
