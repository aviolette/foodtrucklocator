package foodtruck.server.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.dao.TimeSeriesDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.model.StatVector;
import foodtruck.model.SystemStats;
import foodtruck.model.TimeValue;
import foodtruck.model.TruckStop;
import foodtruck.util.FifteenMinuteRollup;
import foodtruck.util.Slots;
import foodtruck.util.WeeklyRollup;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
@Path("/stats")
public class StatsResource {
  private final FifteenMinuteRollupDAO fifteenMinuteRollupDAO;
  private final TruckStopDAO truckStopDAO;
  private final Slots fifteenMinuteRollups;
  private final Slots weeklyRollups;
  private final WeeklyRollupDAO weeklyRollupDAO;

  @Inject
  public StatsResource(FifteenMinuteRollupDAO fifteenMinuteRollupDAO, TruckStopDAO truckStopDAO,
      WeeklyRollupDAO weeklyRollupDAO,
      @FifteenMinuteRollup Slots fifteenMinuteRollups,
      @WeeklyRollup Slots weeklyRollups) {
    this.fifteenMinuteRollupDAO = fifteenMinuteRollupDAO;
    this.truckStopDAO = truckStopDAO;
    this.fifteenMinuteRollups = fifteenMinuteRollups;
    this.weeklyRollups = weeklyRollups;
    this.weeklyRollupDAO = weeklyRollupDAO;
  }

  private TimeSeriesDAO dao(long interval) {
    // TODO: this is a really stupid way to do it
    if (interval == 604800000L) {
      return weeklyRollupDAO;
    } else {
      return fifteenMinuteRollupDAO;
    }
  }

  private Slots slots(long interval) {
    return (interval == 604800000L) ? weeklyRollups : fifteenMinuteRollups;
  }

  @GET @Path("counts/{statList}") @Produces(MediaType.APPLICATION_JSON)
  public JResponse<List<StatVector>> getStatsFor(@PathParam("statList") final String statNames,
      @QueryParam("start") final long startTime, @QueryParam("end") final long endTime,
      @QueryParam("interval") final long interval) {
    String[] statList = statNames.split(",");
    Slots slots = slots(interval);
    if (statList.length > 0 && statList[0].equals("trucksOnRoad")) {
      return trucksOnRoad(startTime, endTime, slots);
    }
    List<SystemStats> stats = dao(interval).findWithinRange(startTime, endTime, statList);
    ImmutableList.Builder<StatVector> builder = ImmutableList.builder();
    for (String statName : statList) {
      builder.add(slots.fillIn(stats, statName, startTime, endTime));
    }
    List<StatVector> statVectors = builder.build();
    return JResponse.ok(statVectors).build();
  }

  private JResponse<List<StatVector>> trucksOnRoad(long startTime, long endTime, Slots slots) {
    StatVector vector = slots.fillIn(ImmutableList.<SystemStats>of(), "trucksOnRoad",
        startTime, endTime);
    List<TruckStop> truckStops =
        truckStopDAO.findOverRange(null, new Interval(startTime, endTime));
    for (TimeValue timeValue : vector.getDataPoints()) {
      // inefficient!!!!
      timeValue.setCount(countWithinRange(truckStops, new DateTime(timeValue.getTimestamp())));
    }
    List<StatVector> statVectors = ImmutableList.of(vector);
    return JResponse.ok(statVectors).build();

  }

  private long countWithinRange(List<TruckStop> truckStops, DateTime dateTime) {
    long count = 0;
    for (TruckStop stop : truckStops) {
      if (stop.activeDuring(dateTime)) {
        count++;
      }
    }
    return count;
  }
}
