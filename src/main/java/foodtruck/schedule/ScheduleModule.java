package foodtruck.schedule;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.gdata.client.calendar.CalendarService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import org.joda.time.DateTimeZone;

import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class ScheduleModule extends AbstractModule {
  @Override
  protected void configure() {
    // TODO: use assisted inject
    bind(CalendarQueryFactory.class).to(CalendarQueryFactoryImpl.class);
  }

  @Provides @Named("calendar.feed.url")
  public URL provideCalendarUrl() throws MalformedURLException {
    return new URL(System.getProperty("calendar.feed.url"));
  }

  @Provides
  public CalendarService provideCalendarService() {
    CalendarService service = new CalendarService("foodtruck-app");
    service.setConnectTimeout(6000);
    return service;
  }

  @Provides
  public GoogleCalendar provideGoogleCalendarStrategy(CalendarService service,
      CalendarQueryFactory queryProvider, DateTimeZone zone, GeoLocator geoLocator,
      TruckDAO truckDAO, AddressExtractor extractor) {
    return new GoogleCalendar(service, queryProvider, zone, geoLocator, truckDAO, extractor);
  }
}

