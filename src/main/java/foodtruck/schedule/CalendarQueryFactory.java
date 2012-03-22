package foodtruck.schedule;

import java.net.URL;

import com.google.gdata.client.calendar.CalendarQuery;

/**
 * Creates CalendarQuery objects.
 * @author aviolette@gmail.com
 * @since 8/28/11
 */
public interface CalendarQueryFactory {
  /**
   * Create a calendar query hooked up with the default end-point
   */
  public CalendarQuery create();


  /**
   * Create calendar query based on the end point passed in
   */
  CalendarQuery create(URL url);
}
