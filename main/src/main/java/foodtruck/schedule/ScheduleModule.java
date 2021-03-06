package foodtruck.schedule;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.script.ScriptEngineManager;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.annotations.MapCenter;
import foodtruck.model.Location;
import foodtruck.schedule.custom.chicago.ChicagoModule;
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
    Multibinder<ScheduleStrategy> connectorBinder = Multibinder.newSetBinder(binder(), ScheduleStrategy.class);
    connectorBinder.addBinding().to(TempTruckStopScheduleStrategy.class);
    bind(SocialMediaCacher.class).to(SocialMediaCacherImpl.class);
    bind(FoodTruckStopService.class).to(FoodTruckStopServiceImpl.class);
    install(new ChicagoModule());
  }

  @Provides
  public ScriptEngineManager provideScriptEngineManager() {
    return new ScriptEngineManager();
  }

  @Provides @MapCenter
  public Location providesMapCenter() {
    Location.Builder builder = Location.builder().name("Unnamed")
        .lat(41.880187)
        .lng(-87.63083499999999);
    try {
      Iterable<String> items = Splitter.on(";")
          .trimResults()
          .split(System.getProperty("foodtrucklocator.center", "Clark and Monroe, Chicago, IL; 41.880187; -87.63083499999999"));
      Iterator<String> it = items.iterator();
      builder.name(it.next());
      builder.lat(Double.parseDouble(it.next()));
      builder.lng(Double.parseDouble(it.next()));
    } catch (Exception ignored) {
    }
    return builder.build();
  }

  @Provides
  @Singleton
  @Named("exemptLocations")
  public List<String> providesExemptLocations() {
    // TODO: probably should make this a system property
    return ImmutableList.of("Flower Truck", "Lincoln Square Open 10-8", "Not-So-Mobile Eatery – After School Special",
        "Where’s Grill Chasers today?", "Pop Up Shop Ogilvie French Market", "Busy");
  }

  @Provides
  @DefaultStartTime
  @Singleton
  public LocalTime provideDefaultStartTime(@MilitaryTimeOnlyFormatter DateTimeFormatter formatter) {
    try {
      LocalTime localTime = new LocalTime(11, 0);
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

