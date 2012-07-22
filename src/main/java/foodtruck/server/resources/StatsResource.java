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

import foodtruck.dao.SystemStatDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.StatVector;
import foodtruck.model.SystemStats;
import foodtruck.model.TimeValue;
import foodtruck.model.TruckStop;
import foodtruck.stats.Slots;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
public class StatsResource {
  private final SystemStatDAO systemStatDAO;
  private final TruckStopDAO truckStopDAO;

  @Inject
  public StatsResource(SystemStatDAO systemStatDAO, TruckStopDAO truckStopDAO) {
    this.systemStatDAO = systemStatDAO;
    this.truckStopDAO = truckStopDAO;
  }

  @GET @Path("counts/{statList}")
  public JResponse<List<StatVector>> getStatsFor(@PathParam("statList") final String statNames,
      @QueryParam("start") final long startTime, @QueryParam("end") final long endTime) {
    String[] statList = statNames.split(",");
    if (statList.length > 0 && statList[0].equals("trucksOnRoad")) {
      return trucksOnRoad(startTime, endTime);
    }
    List<SystemStats> stats = systemStatDAO.findWithinRange(startTime, endTime);
    ImmutableList.Builder<StatVector> builder = ImmutableList.builder();
    for (String statName : statList) {
      builder.add(Slots.fillIn(stats, statName, startTime, endTime));
    }
    List<StatVector> statVectors = builder.build();
    return JResponse.ok(statVectors).build();
  }

  private JResponse<List<StatVector>> trucksOnRoad(long startTime, long endTime) {
    StatVector vector = Slots.fillIn(ImmutableList.<SystemStats>of(), "trucksOnRoad",
        startTime, endTime);
    List<TruckStop> truckStops =
        truckStopDAO.findOverRange(null, new DateTime(startTime), new DateTime(endTime));
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
