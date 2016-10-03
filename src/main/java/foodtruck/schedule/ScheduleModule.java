package foodtruck.schedule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngineManager;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.schedule.custom.AmanecerTacosMatcher;
import foodtruck.schedule.custom.BeaverMatcher;
import foodtruck.schedule.custom.BobChaMatcher;
import foodtruck.schedule.custom.CajunConMatcher;
import foodtruck.schedule.custom.LaJefaMatcher;
import foodtruck.schedule.custom.PierogiWagonMatcher;
import foodtruck.schedule.custom.RoostMatcher;
import foodtruck.util.MilitaryTimeOnlyFormatter;

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
    bind(ScheduleCacher.class).to(MemcacheScheduleCacher.class);
    Multibinder<SpecialMatcher> binder = Multibinder.newSetBinder(binder(), SpecialMatcher.class);
    binder.addBinding().to(BeaverMatcher.class);
    binder.addBinding().to(LaJefaMatcher.class);
    binder.addBinding().to(RoostMatcher.class);
    binder.addBinding().to(BobChaMatcher.class);
    binder.addBinding().to(PierogiWagonMatcher.class);
    binder.addBinding().to(AmanecerTacosMatcher.class);
    binder.addBinding().to(CajunConMatcher.class);
  }

  @Provides @Singleton
  public ImmutableList<Spot> provideCommonSpots() {
    return ImmutableList.of(
        new Spot("600w", "600 West Chicago Avenue, Chicago, IL"),
        new Spot("wabash/vanburen", "Wabash and Van Buren, Chicago, IL"),
        new Spot("wacker/adams", "Wacker and Adams, Chicago, IL"),
        new Spot("clark/adams", "Clark and Adams, Chicago, IL"),
        new Spot("harrison/michigan", "Michigan and Harrison, Chicago, IL"),
        new Spot("lasalle/adams", "Lasalle and Adams, Chicago, IL"),
        new Spot("clark/monroe", "Clark and Monroe, Chicago, IL"),
        new Spot("wabash/jackson", "Wabash and Jackson, Chicago, IL"),
        new Spot("michigan/monroe", "Michigan and Monroe, Chicago, IL"),
        new Spot("uchicago", "University of Chicago"),
        new Spot("uofc", "University of Chicago"),
        new Spot("58th/ellis", "University of Chicago"));
  }

  @Provides
  @Singleton
  @Named("exemptLocations")
  public List<String> providesExemptLocations() {
    // TODO: probably should make this a system property
    return ImmutableList.of("Flower Truck", "Lincoln Square Open 10-8", "Not-So-Mobile Eatery – After School Special");
  }


  @Provides @Singleton
  public MemcacheService provideMemcacheService() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    return syncCache;
  }

  @Provides
  public ScriptEngineManager provideScriptEngineManager() {
    return new ScriptEngineManager();
  }

  @Provides @Named("center")
  public Location providesMapCenter(StaticConfig config) {
    return config.getCenter();
  }

  @Provides @DefaultStartTime @Singleton
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
}

