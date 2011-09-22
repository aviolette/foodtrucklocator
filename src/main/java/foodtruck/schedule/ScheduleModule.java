package foodtruck.schedule;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.google.gdata.client.calendar.CalendarService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.joda.time.DateTimeZone;

import foodtruck.config.TruckConfigParser;
import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Truck;
import twitter4j.TwitterFactory;

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
    return new CalendarService("foodtruck-app");
  }

  @Provides
  public GoogleCalendarStrategy provideGoogleCalendarStrategy(CalendarService service,
      CalendarQueryFactory queryProvider, DateTimeZone zone, GeoLocator geoLocator) {
    return new GoogleCalendarStrategy(service, queryProvider, zone, geoLocator);
  }

  @Provides @Singleton
  public TwitterFactoryWrapper provideTwitterFactory() {
    return new TwitterFactoryWrapper(new TwitterFactory());
  }

  @Provides @Singleton
  public Map<String, Truck> providesTrucks(TruckConfigParser parser)
      throws FileNotFoundException {
    String url =
        Thread.currentThread().getContextClassLoader().getResource("trucks.yaml").getFile();
    return parser.parse(url);
  }
}

