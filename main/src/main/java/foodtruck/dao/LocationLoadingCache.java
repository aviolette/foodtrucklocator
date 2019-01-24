package foodtruck.dao;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 2019-01-24
 */
public class LocationLoadingCache {

  private final LoadingCache<String, Optional<Location>> cache;

  @Inject
  public LocationLoadingCache(LocationDAO locationDAO) {
    cache = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build(new CacheLoader<String, Optional<Location>> () {
          public Optional<Location> load(String name) throws Exception {
            return locationDAO.findByAliasOpt(name);
          }
        });
  }

  public Optional<Location> findLocation(String name) {
    return cache.getUnchecked(name);
  }
}
