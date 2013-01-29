package foodtruck.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import foodtruck.dao.ApplicationDAO;
import foodtruck.model.Application;
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
  private final ApplicationDAO appDAO;

  @Inject
  public DailyScheduleResource(FoodTruckStopService foodTruckService, Clock clock, ApplicationDAO appDAO) {
    this.truckService = foodTruckService;
    this.clock = clock;
    this.appDAO = appDAO;
  }

  @GET @Produces("application/json")
  public DailySchedule findForDay(@QueryParam("appKey") final String appKey) {
    requireAppKey(appKey);
    return truckService.findStopsForDay(clock.currentDay());
  }

  private void requireAppKey(String appKey) {
    if (!Strings.isNullOrEmpty(appKey)) {
      Application app = appDAO.findById(appKey);
      if (app != null && app.isEnabled()) {
        return;
      }
    }
    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
  }
}
