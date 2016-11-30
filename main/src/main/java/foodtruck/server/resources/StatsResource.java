package foodtruck.server.resources;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import org.joda.time.DateTime;

import foodtruck.dao.TimeSeriesDAO;
import foodtruck.model.Slots;
import foodtruck.model.StatVector;
import foodtruck.model.SystemStats;
import foodtruck.monitoring.CounterImpl;
import foodtruck.monitoring.DailyScheduleCounter;
import foodtruck.time.Clock;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
@Path("/stats")
public class StatsResource {
  private static final Logger log = Logger.getLogger(StatsResource.class.getName());
  private static final long DAY_IN_MILLIS = 86400000L;
  private final MemcacheService cache;
  private final CounterImpl scheduleCounter;
  private final Clock clock;
  private final TimeSeriesSelector seriesSelector;

  @Inject
  public StatsResource(TimeSeriesSelector seriesSelector, Clock clock, MemcacheService cache,
      @DailyScheduleCounter CounterImpl counter) {
    this.cache = cache;
    this.clock = clock;
    this.seriesSelector = seriesSelector;
    this.scheduleCounter = counter;
  }

  @GET @Path("counts/{statList}") @Produces(MediaType.APPLICATION_JSON)
  public JResponse<List<StatVector>> getStatsFor(@PathParam("statList") final String statNames,
      @QueryParam("start") final long startTime, @QueryParam("end") final long endTime,
      @QueryParam("interval") final long interval, @QueryParam("nocache") boolean nocache) {
    String[] statList = statNames.split(",");
    //TODO: what if statList empty?
    TimeSeriesDAO timeSeriesDAO = seriesSelector.select(interval, statList[0]);
    Slots slots = timeSeriesDAO.getSlots();
    if (statList.length  == 1 && !nocache) {
      List<StatVector> vectors = (List<StatVector>) cache.get(statList[0]);
      if (vectors != null) {
        log.log(Level.INFO, "Stats for {0} retrieved from cache", statList[0]);
        return JResponse.ok(vectors)
            .build();
      }
    }
    log.log(Level.INFO, "Requested stats: {0}", ImmutableList.copyOf(statList));
    List<SystemStats> stats = timeSeriesDAO.findWithinRange(startTime, endTime, statList);
    ImmutableList.Builder<StatVector> builder = ImmutableList.builder();
    for (String statName : statList) {
      //TODO: hack
      if (statName.startsWith("service.count.daily") && interval == DAY_IN_MILLIS) {
        stats = augmentWithTodaysCounts(stats, statName, slots);
      }
      builder.add(slots.fillIn(stats, statName, startTime, endTime));
    }
    List<StatVector> statVectors = builder.build();
    if (statList.length == 1) {
      log.log(Level.INFO, "Stats for {0} stored in cache", statList[0]);
      cache.put(statList[0], statVectors, Expiration.byDeltaSeconds(86400));
    }
    return JResponse.ok(statVectors).build();
  }

  private List<SystemStats> augmentWithTodaysCounts(List<SystemStats> stats, String statName, Slots slots) {
    // TODO: not efficient
    DateTime now = clock.now();
    ImmutableList.Builder<SystemStats> builder = ImmutableList.builder();
    long timeSlot = slots.getSlot(clock.now().getMillis());
    boolean found = false;
    String suffix = statName.substring(statName.lastIndexOf(".") + 1);
    for (SystemStats stat : stats) {
      if (timeSlot == stat.getTimeStamp()) {
        long count = scheduleCounter.getCount(suffix);
        SystemStats systemStats = new SystemStats((Long)stat.getKey(),timeSlot, ImmutableMap.of(statName, count));
        builder.add(stat.merge(systemStats));
        found = true;
      } else {
        builder.add(stat);
      }
    }
    if (!found) {
      long count = scheduleCounter.getCount(suffix);
      builder.add(new SystemStats(0, timeSlot, ImmutableMap.of(statName, count)));
    }
    return builder.build();
  }
}
