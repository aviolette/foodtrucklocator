package foodtruck.schedule;

import java.util.logging.Level;

import javax.script.ScriptEngineManager;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import foodtruck.model.Location;
import foodtruck.model.StaticConfig;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class ScheduleModule extends AbstractModule {
  @Override
  protected void configure() {
    // TODO: use assisted inject
    bind(AddressExtractor.class).to(JavascriptAddressExtractor.class);
    bind(ScheduleStrategy.class).to(GoogleCalendarV3Consumer.class);
    bind(ScheduleCacher.class).to(MemcacheScheduleCacher.class);
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
}

