package foodtruck.schedule;

import java.util.List;
import java.util.logging.Logger;

import javax.script.ScriptEngineManager;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.schedule.custom.chicago.ChicagoModule;
import foodtruck.schedule.custom.nyc.NewYorkModule;
import foodtruck.time.MilitaryTimeOnlyFormatter;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class ScheduleModule extends AbstractModule {
  private static final Logger log = Logger.getLogger(ScheduleModule.class.getName());

  @Override
  protected void configure() {
    bind(AddressExtractor.class).to(JavascriptAddressExtractor.class);
    bind(ScheduleStrategy.class).to(GoogleCalendarV3Consumer.class);
    String city = System.getProperty("foodtrucklocator.city", "Chicago");
    if ("New York".equals(city)) {
      install(new NewYorkModule());
    } else {
      install(new ChicagoModule());
    }
  }

  @Provides
  public ScriptEngineManager provideScriptEngineManager() {
    return new ScriptEngineManager();
  }

  @Provides
  @Named("center")
  public Location providesMapCenter(StaticConfig config) {
    return config.getCenter();
  }

  @Provides
  @Singleton
  @Named("exemptLocations")
  public List<String> providesExemptLocations() {
    // TODO: probably should make this a system property
    return ImmutableList.of("Flower Truck", "Lincoln Square Open 10-8", "Not-So-Mobile Eatery – After School Special",
        "Where’s Grill Chasers today?", "Pop Up Shop Ogilvie French Market");
  }

  @Provides
  @DefaultStartTime
  @Singleton
  public LocalTime provideDefaultStartTime(@MilitaryTimeOnlyFormatter DateTimeFormatter formatter) {
    try {
      LocalTime localTime = formatter.parseLocalTime(System.getProperty("foodtrucklocator.lunchtime", "11:30"));
      log.info("Lunch time is at: " + localTime);
      return localTime;
    } catch (Exception e) {
      log.severe(e.getMessage());
    }
    return new LocalTime(11, 30);
  }


  @Singleton
  @Provides
  public Calendar providesCalendar(HttpTransport httpTransport, JsonFactory jsonFactory,
      @javax.inject.Named("projectId") String applicationName, HttpRequestInitializer credential) {
    return new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName(applicationName)
        .build();
  }

}

