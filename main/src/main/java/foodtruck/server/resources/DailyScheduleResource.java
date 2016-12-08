package foodtruck.server.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.google.api.client.util.Strings;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;

import foodtruck.monitoring.Counter;
import foodtruck.monitoring.DailyScheduleCounter;
import foodtruck.monitoring.HourlyScheduleCounter;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.schedule.ScheduleCacher;
import foodtruck.server.resources.json.DailyScheduleWriter;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 1/27/13
 */
@Path("/daily_schedule") @Produces(MediaType.APPLICATION_JSON)
public class DailyScheduleResource {
  private static final Logger log = Logger.getLogger(DailyScheduleResource.class.getName());
  private final FoodTruckStopService truckService;
  private final Clock clock;
  private final AuthorizationChecker checker;
  private final ScheduleCacher scheduleCacher;
  private final DailyScheduleWriter dailyScheduleWriter;
  private final Counter dailyCounter;
  private final Counter hourlyCounter;

  @Inject
  public DailyScheduleResource(FoodTruckStopService foodTruckService, Clock clock, AuthorizationChecker checker,
      ScheduleCacher scheduleCacher, DailyScheduleWriter writer, @DailyScheduleCounter Counter counter,
      @HourlyScheduleCounter Counter hourlyCounter) {
    this.truckService = foodTruckService;
    this.clock = clock;
    this.checker = checker;
    this.scheduleCacher = scheduleCacher;
    this.dailyScheduleWriter = writer;
    this.dailyCounter = counter;
    this.hourlyCounter = hourlyCounter;
  }

  @GET @Produces("application/json")
  public String findForDay(@QueryParam("appKey") final String appKey, @QueryParam("from") final long from,
      @QueryParam("for") String aDate) {
    dailyCounter.increment(appKey);
    hourlyCounter.increment(appKey);
    checker.requireAppKeyWithCount(appKey, hourlyCounter.getCount(appKey), dailyCounter.getCount(appKey));
    if (!Strings.isNullOrEmpty(aDate)) {
      // TODO: should definitely validate that aDate is tomorrow before saving it in cache
      log.info("Pulling schedule for day: " + aDate);
      return scheduleCacher.findTomorrowsSchedule();
    } else if (from > 0) {
      try {
        return dailyScheduleWriter.asJSON(truckService.findStopsForDayAfter(new DateTime(from, clock.zone()))).toString();
      } catch (JSONException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
        throw new WebApplicationException(500);
      }
    }
    return scheduleCacher.findSchedule();
  }
}
