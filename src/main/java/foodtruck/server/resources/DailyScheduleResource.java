package foodtruck.server.resources;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Throwables;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;

import foodtruck.schedule.ScheduleCacher;
import foodtruck.server.resources.json.DailyScheduleWriter;
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
  private final ScheduleCacher scheduleCacher;
  private final DailyScheduleWriter dailyScheduleWriter;
  private static final Logger log = Logger.getLogger(DailyScheduleResource.class.getName());

  @Inject
  public DailyScheduleResource(FoodTruckStopService foodTruckService, Clock clock, AuthorizationChecker checker,
      ScheduleCacher scheduleCacher, DailyScheduleWriter writer) {
    this.truckService = foodTruckService;
    this.clock = clock;
    this.checker = checker;
    this.scheduleCacher = scheduleCacher;
    this.dailyScheduleWriter = writer;
  }

  @GET @Produces("application/json")
  public String findForDay(@QueryParam("appKey") final String appKey, @QueryParam("from") final long from) {
    checker.requireAppKey(appKey);
    if (from > 0) {
      try {
        return dailyScheduleWriter.asJSON(truckService.findStopsForDayAfter(new DateTime(from, clock.zone()))).toString();
      } catch (JSONException e) {
        throw Throwables.propagate(e);
      }
    }
    String payload = scheduleCacher.findSchedule();
    if (payload == null) {
      try {
        log.info("Pulled schedule from db");
        payload = dailyScheduleWriter.asJSON(truckService.findStopsForDay(clock.currentDay())).toString();
        scheduleCacher.saveSchedule(payload);
        return payload;
      } catch (JSONException e) {
        throw Throwables.propagate(e);
      }
    }
    log.info("Pulled schedule from cache");
    return payload;
  }
}
