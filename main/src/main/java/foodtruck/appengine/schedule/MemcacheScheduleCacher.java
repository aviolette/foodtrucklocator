package foodtruck.appengine.schedule;

import javax.annotation.Nullable;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;

import foodtruck.truckstops.ScheduleCacher;

/**
 * @author aviolette
 * @since 5/29/13
 */
public class MemcacheScheduleCacher implements ScheduleCacher {
  private static final String DAILY_SCHEDULE = "daily_schedule";
  private static final String TOMORROWS_SCHEDULE = "tomorrows_schedule";
  private final MemcacheService cache;

  @Inject
  public MemcacheScheduleCacher(MemcacheService cache) {
    this.cache = cache;
  }
  @Override public @Nullable String findSchedule() {
    return (String) cache.get(DAILY_SCHEDULE);
  }

  @Override public void saveSchedule(String payload) {
    cache.put(DAILY_SCHEDULE, payload, Expiration.byDeltaSeconds(300));
  }

  @Override public void invalidate() {
    cache.delete(DAILY_SCHEDULE);
    cache.delete(TOMORROWS_SCHEDULE);
  }

  @Override
  public @Nullable String findTomorrowsSchedule() {
    return (String) cache.get(TOMORROWS_SCHEDULE);
  }

  @Override public void saveTomorrowsSchedule(String payload) {
    cache.put(TOMORROWS_SCHEDULE, payload, Expiration.byDeltaSeconds(3600));
  }
}
