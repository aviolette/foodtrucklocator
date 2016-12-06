package foodtruck.appengine.monitoring;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import foodtruck.linxup.ErrorCounter;
import foodtruck.monitoring.CommonMonitoringModule;
import foodtruck.monitoring.Counter;
import foodtruck.monitoring.CounterPublisher;
import foodtruck.monitoring.DailyScheduleCounter;
import foodtruck.monitoring.HourlyScheduleCounter;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
public class MonitoringModule extends AbstractModule {

  @Override protected void configure() {
    bind(CounterPublisher.class).to(QueuePublisher.class);
    install(new CommonMonitoringModule());
  }

  @Provides
  @DailyScheduleCounter
  public Counter providesDailyScheduleCounter(MemcacheService memcacheService) {
    return new CounterImpl("service.access.daily", memcacheService, null);
  }

  @Provides @HourlyScheduleCounter
  public Counter providesHourlyScheduleCounter(MemcacheService memcacheService) {
    return new CounterImpl("service.access.hourly", memcacheService, Expiration.byDeltaSeconds(3600));
  }

  @Provides
  @ErrorCounter
  public Counter providesServiceErrorCounter(MemcacheService memcacheService) {
    return new CounterImpl("service.trackingdeviceservice.error", memcacheService, Expiration.byDeltaSeconds(3600));
  }
}
