package foodtruck.schedule;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.base.Throwables;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.inject.Inject;

import foodtruck.dao.ConfigurationDAO;

/**
 * @author aviolette@gmail.com
 * @since 8/28/11
 */
public class CalendarQueryFactoryImpl implements CalendarQueryFactory {
  private final ConfigurationDAO configDAO;

  @Inject
  public CalendarQueryFactoryImpl(ConfigurationDAO configDAO) {
    this.configDAO = configDAO;
  }

  @Override public CalendarQuery create() {
    try {
      return new CalendarQuery(new URL(configDAO.find().getGoogleCalendarAddress()));
    } catch (MalformedURLException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override public CalendarQuery create(URL url) {
    return new CalendarQuery(url);
  }
}
