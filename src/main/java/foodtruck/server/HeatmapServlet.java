package foodtruck.server;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 4/30/14
 */
@Singleton
public class HeatmapServlet extends FrontPageServlet {
  private static final String JSP = "/WEB-INF/jsp/heatmap.jsp";
  private static final String CACHE_KEY = "30daystats";
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;
  private final MemcacheService cache;

  @Inject
  public HeatmapServlet(ConfigurationDAO configDAO, TruckStopDAO truckStopDAO, Clock clock, MemcacheService cache) {
    super(configDAO);
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
    this.cache = cache;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // TODO: Add this to cache
    DateTime now = clock.now();
    Interval interval = new Interval(clock.now().minusMonths(3), now);

    String locations;
    if (cache.contains(CACHE_KEY)) {
      locations = (String) cache.get(CACHE_KEY);
    } else {
      locations = "[" + Joiner.on(",").join(FluentIterable.from(truckStopDAO.findOverRange(null, interval))
          .transform(new Function<TruckStop, String>() {
            @Override public String apply(TruckStop truckStop) {
              return "new google.maps.LatLng(" + truckStop.getLocation().getLatitude() + "," + truckStop.getLocation().getLongitude() + ")";
            }
          }).toList()) + "]";
      cache.put(CACHE_KEY, locations, Expiration.byDeltaSeconds(60 * 60 * 24));
    }

    req.setAttribute("locations", locations);
    req.setAttribute("center", configurationDAO.find().getCenter());
    req.getRequestDispatcher(JSP).forward(req, resp);
  }
}
