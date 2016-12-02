package foodtruck.schedule;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import foodtruck.caching.Cacher;

/**
 * @author aviolette
 * @since 5/29/13
 */
public class MemcacheScheduleCacher implements ScheduleCacher {
  private static final String DAILY_SCHEDULE = "daily_schedule";
  private static final String TOMORROWS_SCHEDULE = "tomorrows_schedule";
  private Cacher cache;

  @Inject
  public MemcacheScheduleCacher(Cacher cache) {
    this.cache = cache;
  }
  @Override public @Nullable String findSchedule() {
    return (String) cache.get(DAILY_SCHEDULE);
  }

  @Override public void saveSchedule(String payload) {
    cache.put(DAILY_SCHEDULE, payload, 5);
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
    cache.put(TOMORROWS_SCHEDULE, payload, 5);
  }
}
