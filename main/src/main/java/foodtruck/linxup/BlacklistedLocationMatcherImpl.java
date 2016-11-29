package foodtruck.linxup;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.TrackingDevice;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 11/22/16
 */
public class BlacklistedLocationMatcherImpl implements BlacklistedLocationMatcher {
  private static final Logger log = Logger.getLogger(BlacklistedLocationMatcherImpl.class.getName());

  private final LoadingCache<String, List<Location>> blacklistCache;

  @Inject
  public BlacklistedLocationMatcherImpl(final TruckDAO truckDAO, final LocationDAO locationDAO) {
    blacklistCache = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build(new CacheLoader<String, List<Location>>() {
          public List<Location> load(String key) throws Exception {
            Truck truck = truckDAO.findById(key);
            //noinspection ConstantConditions,ConstantConditions
            return FluentIterable.from(truck.getBlacklistLocationNames())
                .transform(new Function<String, Location>() {
                  public Location apply(String name) {
                    return locationDAO.findByAddress(name);
                  }
                })
                .filter(Predicates.<Location>notNull())
                .toList();
          }
        });
  }


  @Override
  public boolean isBlacklisted(@Nullable TrackingDevice device) {
    if (device == null || device.getLastLocation() == null || Strings.isNullOrEmpty(device.getTruckOwnerId())) {
      return false;
    }
    List<Location> locations;
    try {
      locations = blacklistCache.get(device.getTruckOwnerId());
    } catch (ExecutionException e) {
      log.log(Level.WARNING, e.getMessage(), e);
      return false;
    }
    if (locations == null) {
      return false;
    }
    for (Location location : locations) {
      if (location.within(location.getRadius())
          .milesOf(device.getLastLocation())) {
        return true;
      }
    }
    return false;
  }
}
