package foodtruck.server.job;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.util.Strings;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.util.Clock;
import foodtruck.util.DateOnlyFormatter;

/**
 * @author aviolette
 * @since 3/9/15
 */
@Singleton
public class UpdateLocationStats extends HttpServlet {
  private static final Logger log = Logger.getLogger(UpdateLocationStats.class.getName());
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;
  private final WeeklyRollupDAO rollupDAO;
  private final LocationDAO locationDAO;
  private final DateTimeFormatter formatter;

  @Inject
  public UpdateLocationStats(TruckStopDAO truckStopDAO, Clock clock, LocationDAO locationDAO,
      WeeklyRollupDAO rollupDAO, @DateOnlyFormatter DateTimeFormatter formatter) {
    this.clock = clock;
    this.locationDAO = locationDAO;
    this.rollupDAO = rollupDAO;
    this.truckStopDAO = truckStopDAO;
    this.formatter = formatter;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // cache is used since the name stored in the stop collection might not be the canonical name (i.e. the name
    // at the top-level of the alias tree)
    LoadingCache<String, Location> cache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .build(new CacheLoader<String, Location>() {
          public Location load(String locationName) throws Exception {
            return locationDAO.findByAddress(locationName);
          }
        });
    Interval range = determineRange(req);
    log.info("Updating location stats over range: " + range);
    for (TruckStop stop : truckStopDAO.findOverRange(null, range)) {
      try {
        String name = stop.getLocation().getName();
        if (Strings.isNullOrEmpty(name)) {
          // not sure if this is possible, but it might be true for older stops
          continue;
        }
        Location location = cache.get(stop.getLocation()
              .getName());
        if (!location.getName().equals(name)) {
          log.log(Level.INFO, "Corrected location {0} to {1}", new Object[] {name, location.getName()});
        }
        rollupDAO.updateCount(stop.getStartTime(), "count.location." + location.getKey(), 1);
      } catch (Exception e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
    }
    log.info("Finished updating stats");
  }

  private Interval determineRange(HttpServletRequest req) {
    String range = req.getParameter("range");
    if (Strings.isNullOrEmpty(range)) {
      DateTime now = clock.now();
      return new Interval(now.minusDays(1), now);
    } else {
      String rangeValues[] = range.split("-");
      return new Interval(formatter.parseDateTime(rangeValues[0]), formatter.parseDateTime(rangeValues[1]));
    }
  }
}
