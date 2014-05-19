package foodtruck.stats;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import foodtruck.dao.TruckStopDAO;
import foodtruck.model.TruckStop;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 5/18/14
 */
public class HeatmapServiceImpl implements HeatmapService {
  public static final String CACHE_KEY = "30daystats";
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;
  private final MemcacheService cache;

  @Inject
  public HeatmapServiceImpl(TruckStopDAO truckStopDAO, Clock clock, MemcacheService cache) {
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
    this.cache = cache;
  }

  @Override public String get() {
    if (!cache.contains(CACHE_KEY)) {
      rebuild();
    }
    return (String) cache.get(CACHE_KEY);
  }

  @Override public void rebuild() {
    DateTime now = clock.now();
    Interval interval = new Interval(clock.now().minusMonths(3), now);
    String locations;
    locations = "[" + Joiner.on(",").join(FluentIterable.from(truckStopDAO.findOverRange(null, interval))
        .transform(new Function<TruckStop, String>() {
          @Override public String apply(TruckStop truckStop) {
            return "new google.maps.LatLng(" + truckStop.getLocation().getLatitude() + "," + truckStop.getLocation()
                .getLongitude() + ")";
          }
        }).toList()) + "]";
    cache.put(CACHE_KEY, locations, Expiration.byDeltaSeconds(60 * 60 * 24));
  }
}
