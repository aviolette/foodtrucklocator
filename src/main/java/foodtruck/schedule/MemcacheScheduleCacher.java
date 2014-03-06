package foodtruck.schedule;

import javax.annotation.Nullable;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;

/**
 * @author aviolette
 * @since 5/29/13
 */
public class MemcacheScheduleCacher implements ScheduleCacher {
  public static final String DAILY_SCHEDULE = "daily_schedule";
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
  }
}
