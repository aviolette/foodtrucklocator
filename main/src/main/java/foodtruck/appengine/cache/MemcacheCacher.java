package foodtruck.appengine.cache;

import javax.annotation.Nullable;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;

import org.joda.time.DateTime;

import foodtruck.caching.Cacher;

/**
 * @author aviolette
 * @since 12/2/16
 */
public class MemcacheCacher implements Cacher {
  private final MemcacheService service;

  @Inject
  public MemcacheCacher(MemcacheService service) {
    this.service = service;
  }

  @Override
  public void put(String name, @Nullable Object value, int expirationInMinutes) {
    service.put(name, value, Expiration.byDeltaSeconds(expirationInMinutes * 60));
  }

  @Override
  public void put(String name, @Nullable Object value, DateTime expirationDate) {
    service.put(name, value, Expiration.onDate(expirationDate.toDate()));
  }

  @Nullable
  @Override
  public Object get(String name) {
    return service.get(name);
  }

  @Override
  public boolean contains(String name) {
    return service.contains(name);
  }

  @Override
  public boolean delete(String name) {
    return service.delete(name);
  }
}
