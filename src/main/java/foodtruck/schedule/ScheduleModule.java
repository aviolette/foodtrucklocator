package foodtruck.schedule;

import java.util.logging.Level;

import javax.script.ScriptEngineManager;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gdata.client.calendar.CalendarService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class ScheduleModule extends AbstractModule {
  @Override
  protected void configure() {
    // TODO: use assisted inject
    bind(CalendarQueryFactory.class).to(CalendarQueryFactoryImpl.class);
    bind(AddressExtractor.class).to(JavascriptAddressExtractor.class);
    bind(ScheduleStrategy.class).to(GoogleCalendar.class);
    bind(ScheduleCacher.class).to(MemcacheScheduleCacher.class);
  }

  @Provides @Singleton
  public MemcacheService provideMemcacheService() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    return syncCache;
  }

  @Provides
  public CalendarService provideCalendarService() {
    CalendarService service = new CalendarService("foodtruck-app");
    service.setConnectTimeout(6000);
    return service;
  }

  @Provides
  public ScriptEngineManager provideScriptEngineManager() {
    return new ScriptEngineManager();
  }
}

