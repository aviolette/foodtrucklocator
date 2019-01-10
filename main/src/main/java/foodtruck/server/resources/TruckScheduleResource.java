package foodtruck.server.resources;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.model.TruckSchedule;
import foodtruck.monitoring.Monitored;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.server.security.SecurityChecker;
import foodtruck.time.Clock;

import static foodtruck.server.resources.Resources.noCache;

/**
 * @author aviolette@gmail.com
 * @since 10/15/12
 */
@Path("/schedule")
public class TruckScheduleResource {
  private static final Logger log = Logger.getLogger(TruckScheduleResource.class.getName());
  private final FoodTruckStopService stopService;
  private final Clock clock;
  private final SecurityChecker securityChecker;

  @Inject
  public TruckScheduleResource(FoodTruckStopService stopService, Clock clock, SecurityChecker securityChecker) {
    this.stopService = stopService;
    this.clock = clock;
    this.securityChecker = securityChecker;
  }

  @GET
  @Path("{truckId}")
  @Monitored
  public JResponse<TruckSchedule> findSchedule(@PathParam("truckId") String truckId) {
    securityChecker.requiresLoggedInAs(truckId);
    try {
      return noCache(JResponse.ok(stopService.findStopsForDay(truckId, clock.currentDay()))).build();
    } catch (IllegalStateException ise) {
      log.warning(ise.getMessage());
      throw new WebApplicationException(404);
    }
  }
}
