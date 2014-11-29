package foodtruck.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import foodtruck.dao.TruckStopDAO;
import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

import static foodtruck.server.resources.Resources.requiresAdmin;

/**
 * @author aviolette
 * @since 11/29/14
 */
@Produces("text/csv")
@Path("/truckstops")
public class RawTruckStopsResource {
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;

  @Inject
  public RawTruckStopsResource(TruckStopDAO truckStopDAO, Clock clock) {
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
  }

  @GET
  public Iterable<TruckStop> findResourcesOverThreeMonths() {
    requiresAdmin();
    DateTime end = clock.now(), start = end.minusDays(30);
    Interval interval = new Interval(start, end);
    return truckStopDAO.findOverRange(null, interval);
  }
}
