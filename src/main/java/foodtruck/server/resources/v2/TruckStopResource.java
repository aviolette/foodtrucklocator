package foodtruck.server.resources.v2;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Inject;

import org.joda.time.DateTime;

import foodtruck.dao.TruckStopDAO;
import foodtruck.model.TruckStop;
import foodtruck.server.security.SecurityChecker;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;


/**
 * @author aviolette
 * @since 3/30/15
 */
@Path("/v2/stops")
@Produces("application/json")
public class TruckStopResource {
  private final FoodTruckStopService foodTruckService;
  private final Clock clock;
  private final SecurityChecker checker;
  private final TruckStopDAO truckStopDAO;

  @Inject
  public TruckStopResource(FoodTruckStopService service, Clock clock, SecurityChecker checker,
      TruckStopDAO truckStopDAO) {
    this.foodTruckService = service;
    this.clock = clock;
    this.checker = checker;
    this.truckStopDAO = truckStopDAO;
  }

  @DELETE
  @Path("{stopId: \\d+}")
  public void delete(@PathParam("stopId") final long stopId)
      throws ServletException, IOException {
    if (!checker.isAdmin()) {
      TruckStop stop = foodTruckService.findById(stopId);
      if (stop == null) {
        return;
      }
      checker.requiresLoggedInAs(stop.getTruck().getId());
    }
    foodTruckService.delete(stopId);
  }

  @PUT
  public void save(TruckStop truckStop)
      throws ServletException, IOException {
    checker.requiresLoggedInAs(truckStop.getTruck().getId());
    foodTruckService.update(truckStop, UserServiceFactory.getUserService().getCurrentUser().getEmail());
  }

  @GET
  public Collection<TruckStop> getStops(@QueryParam("truck") String truckId, @Context DateTime startTime) {
    startTime = (startTime == null) ? clock.currentDay().toDateMidnight().toDateTime() : startTime;
    return truckStopDAO.findAfter(truckId, startTime);
  }
}
