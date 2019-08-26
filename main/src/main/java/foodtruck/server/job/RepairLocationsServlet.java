package foodtruck.server.job;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.annotations.Google;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.LocationLoadingCache;
import foodtruck.dao.TruckStopDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2019-01-24
 */
@Singleton
public class RepairLocationsServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(RepairLocationsServlet.class.getName());

  private final TruckStopDAO stopDAO;
  private final Clock clock;
  private final LocationLoadingCache locations;
  private final GeoLocator geoLocator;
  private final LocationDAO locationDAO;

  @Inject
  public RepairLocationsServlet(Clock clock, TruckStopDAO stopDAO, LocationDAO locationDAO,
      @Google GeoLocator geoLocator, LocationLoadingCache locations) {
    this.stopDAO = stopDAO;
    this.clock = clock;
    this.locations = locations;
    this.geoLocator = geoLocator;
    this.locationDAO = locationDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LocalDate now = clock.currentDay();
    LocalDate startDate = now.minusDays(2);
    List<TruckStop> stops = stopDAO.findOverRange(null, new Interval(startDate.toDateTimeAtStartOfDay(clock.zone()),
        clock.currentDay().toDateTimeAtStartOfDay(clock.zone())));
    stops.stream()
        .map(stop -> stop.getLocation().getName())
        .distinct()
        .forEach(locationName -> locations.findLocation(locationName)
            .ifPresent(location -> {
              if (Strings.isNullOrEmpty(location.getCity())) {
                geoLocator.broadSearch(location.getName())
                    .ifPresent(newLocation -> {
                      if (!Strings.isNullOrEmpty(newLocation.getCity())) {
                        Location loc = Location.builder(location)
                            .neighborhood(newLocation.getNeighborhood())
                            .city(newLocation.getCity())
                            .build();
                        log.log(Level.INFO, "Updating location data: {0}", loc);
                        locationDAO.save(loc);
                        locations.invalidate(location);
                      }
                    });
              }
            }));
  }
}
