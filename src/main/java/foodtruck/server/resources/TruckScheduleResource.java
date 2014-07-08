package foodtruck.server.resources;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.dao.TruckDAO;
import foodtruck.model.TruckSchedule;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import static foodtruck.server.resources.Resources.noCache;
import static foodtruck.server.resources.Resources.requiresAdmin;

/**
 * @author aviolette@gmail.com
 * @since 10/15/12
 */
@Path("/schedule")
public class TruckScheduleResource {
  private final FoodTruckStopService stopService;
  private final Clock clock;
  private final TruckDAO dao;
  private static final Logger log = Logger.getLogger(TruckScheduleResource.class.getName());

  @Inject
  public TruckScheduleResource(FoodTruckStopService stopService, Clock clock, TruckDAO dao) {
    this.stopService = stopService;
    this.clock = clock;
    this.dao = dao;
  }

  @GET @Path("{truckId}")
  public JResponse<TruckSchedule> findSchedule(@PathParam("truckId") String truckId) {
    requiresAdmin();
    try {
      return noCache(JResponse.ok(stopService.findStopsForDay(truckId, clock.currentDay()))).build();
    } catch (IllegalStateException ise) {
      log.warning(ise.getMessage());
      throw new WebApplicationException(404);
    }
  }
}
