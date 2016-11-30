package foodtruck.appengine.monitoring;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;

import foodtruck.monitoring.Counter;

/**
 * @author aviolette
 * @since 6/13/15
 */

public class CounterImpl implements Counter {
  private static final Logger log = Logger.getLogger(CounterImpl.class.getName());
  private final MemcacheService memcacheService;
  private final String name;
  private final @Nullable Expiration expiration;

  @Inject
  public CounterImpl(String name, MemcacheService memcacheService, @Nullable Expiration expiration) {
    this.name = name;
    this.memcacheService = memcacheService;
    this.expiration = expiration;
  }

  public long getCount(String suffix) {
    return getCountWithName(name + "." + suffix);
  }

  private long getCountWithName(String fullName) {
    if (memcacheService.contains(fullName)) {
      try {
        return (Long) memcacheService.get(fullName);
      } catch (NullPointerException npe) {
        log.log(Level.WARNING, "NPE SHOULDN'T HAPPEN {0} {1}", new Object[] {fullName, name});
        return 0;
      }
    }
    return 0;
  }

  public void increment(String suffix) {
    incrementWithFullName(name + "." + suffix);
  }

  private void incrementWithFullName(String fullName) {
    if (!memcacheService.contains(fullName)) {
      memcacheService.put(fullName, 1L, expiration);
    } else {
      memcacheService.increment(fullName, 1L);
    }
  }

  public void clear(String suffix) {
    memcacheService.put(name + "." + suffix, 0L);
  }
}
