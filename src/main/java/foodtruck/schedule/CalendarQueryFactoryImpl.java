package foodtruck.schedule;

import java.net.URL;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author aviolette@gmail.com
 * @since 8/28/11
 */
public class CalendarQueryFactoryImpl implements CalendarQueryFactory {
  private final URL feedUrl;

  @Inject
  public CalendarQueryFactoryImpl(@Named("calendar.feed.url") URL feedUrl) {
    this.feedUrl = feedUrl;
  }

  @Override public CalendarQuery create() {
    return new CalendarQuery(feedUrl);
  }

  @Override public CalendarQuery create(URL url) {
    return new CalendarQuery(url);
  }
}
