package foodtruck.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;

import foodtruck.model.DailySchedule;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 1/27/13
 */
@Path("/daily_schedule") @Produces(MediaType.APPLICATION_JSON)
public class DailyScheduleResource {
  private final FoodTruckStopService truckService;
  private final Clock clock;
  private final AuthorizationChecker checker;

  @Inject
  public DailyScheduleResource(FoodTruckStopService foodTruckService, Clock clock, AuthorizationChecker checker) {
    this.truckService = foodTruckService;
    this.clock = clock;
    this.checker = checker;
  }

  @GET @Produces("application/json")
  public DailySchedule findForDay(@QueryParam("appKey") final String appKey) {
    checker.requireAppKey(appKey);
    return truckService.findStopsForDay(clock.currentDay());
  }
}
