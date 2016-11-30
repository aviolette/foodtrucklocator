package foodtruck.linxup;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.dao.TruckStopDAO;
import foodtruck.model.TruckStop;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 11/22/16
 */
class TruckStopLoadingCache implements TruckStopCache {
  private static final Logger log = Logger.getLogger(TruckStopLoadingCache.class.getName());

  private final LoadingCache<String, List<TruckStop>> cache;

  @Inject
  public TruckStopLoadingCache(final TruckStopDAO truckStopDAO, final Clock clock) {
    this.cache = CacheBuilder.newBuilder()
        .build(new CacheLoader<String, List<TruckStop>>() {
          @SuppressWarnings("NullableProblems")
          public List<TruckStop> load(String truckId) throws Exception {
            return truckStopDAO.findDuring(truckId, clock.currentDay());
          }
        });
  }

  @Override
  public List<TruckStop> get(String truckId) {
    try {
      return cache.get(truckId);
    } catch (ExecutionException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return ImmutableList.of();
    }
  }
}
