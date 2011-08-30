package foodtruck.schedule;

import com.google.gdata.client.calendar.CalendarQuery;

/**
 * Creates CalendarQuery objects.
 * @author aviolette@gmail.com
 * @since 8/28/11
 */
public interface CalendarQueryFactory {
  public CalendarQuery create();
}
