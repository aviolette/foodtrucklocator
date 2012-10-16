package foodtruck.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.model.TruckSchedule;
import static foodtruck.server.resources.Resources.noCache;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since 10/15/12
 */
@Path("/schedule")
public class TruckScheduleResource {
  private final FoodTruckStopService stopService;
  private final Clock clock;

  @Inject
  public TruckScheduleResource(FoodTruckStopService stopService, Clock clock) {
    this.stopService = stopService;
    this.clock = clock;
  }

  @GET @Path("{truckId}")
  public JResponse<TruckSchedule> findSchedule(@PathParam("truckId") String truckId) {
    return noCache(JResponse.ok(stopService.findStopsForDay(truckId, clock.currentDay()))).build();
  }
}
