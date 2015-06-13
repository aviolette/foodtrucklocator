package foodtruck.monitoring;

import javax.annotation.Nullable;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;

/**
 * @author aviolette
 * @since 6/13/15
 */

public class Counter {
  private final MemcacheService memcacheService;
  private final String name;
  private final @Nullable Expiration expiration;

  @Inject
  public Counter(String name, MemcacheService memcacheService, @Nullable Expiration expiration) {
    this.name = name;
    this.memcacheService = memcacheService;
    this.expiration = expiration;
  }

  public long getCount(String suffix) {
    return getCountWithName(name + "." + suffix);
  }

  private long getCountWithName(String fullName) {
    return memcacheService.contains(fullName) ? (Long)memcacheService.get(fullName) : 0L;
  }

  public void increment(String suffix) {
    incrementWithFullName(name + "." + suffix);
  }

  private void incrementWithFullName(String fullName) {
    if (expiration != null && !memcacheService.contains(fullName)) {
      memcacheService.put(fullName, 1L, expiration);
    } else {
      memcacheService.increment(fullName, 1);
    }
  }
}
