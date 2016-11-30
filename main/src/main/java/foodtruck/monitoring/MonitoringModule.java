package foodtruck.monitoring;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
public class MonitoringModule extends AbstractModule {
  @Override protected void configure() {
    MonitorInterceptor interceptor = new MonitorInterceptor();
    requestInjection(interceptor);
    bindInterceptor(Matchers.any(),
        Matchers.annotatedWith(Monitored.class), interceptor);
  }

  @Provides
  @DailyScheduleCounter
  public CounterImpl providesDailyScheduleCounter(MemcacheService memcacheService) {
    return new CounterImpl("service.access.daily", memcacheService, null);
  }

  @Provides @HourlyScheduleCounter
  public CounterImpl providesHourlyScheduleCounter(MemcacheService memcacheService) {
    return new CounterImpl("service.access.hourly", memcacheService, Expiration.byDeltaSeconds(3600));
  }
}
