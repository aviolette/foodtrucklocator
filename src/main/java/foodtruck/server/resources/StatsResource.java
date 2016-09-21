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
import org.joda.time.Interval;

import foodtruck.dao.DailyRollupDAO;
import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.dao.TimeSeriesDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.WeeklyLocationStatsRollupDAO;
import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.model.StatVector;
import foodtruck.model.SystemStats;
import foodtruck.model.TimeValue;
import foodtruck.model.TruckStop;
import foodtruck.monitoring.Counter;
import foodtruck.monitoring.DailyScheduleCounter;
import foodtruck.util.Clock;
import foodtruck.util.DailyRollup;
import foodtruck.util.FifteenMinuteRollup;
import foodtruck.util.Slots;
import foodtruck.util.WeeklyRollup;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
@Path("/stats")
public class StatsResource {
  private static final Logger log = Logger.getLogger(StatsResource.class.getName());
  private static final long DAY_IN_MILLIS = 86400000L;
  private final FifteenMinuteRollupDAO fifteenMinuteRollupDAO;
  private final TruckStopDAO truckStopDAO;
  private final Slots fifteenMinuteRollups;
  private final Slots weeklyRollups;
  private final WeeklyRollupDAO weeklyRollupDAO;
  private final WeeklyLocationStatsRollupDAO weeklyLocationStatsRollupDAO;
  private final DailyRollupDAO dailyRollupDAO;
  private final MemcacheService cache;
  private final Slots dailyRollups;
  private final Counter scheduleCounter;
  private final Clock clock;

  @Inject
  public StatsResource(FifteenMinuteRollupDAO fifteenMinuteRollupDAO, TruckStopDAO truckStopDAO,
      WeeklyRollupDAO weeklyRollupDAO, WeeklyLocationStatsRollupDAO weeklyLocationStatsRollupDAO,
      @FifteenMinuteRollup Slots fifteenMinuteRollups, @WeeklyRollup Slots weeklyRollups, MemcacheService cache,
      DailyRollupDAO dailyRollupDAO, @DailyRollup Slots dailyRollups, @DailyScheduleCounter Counter counter,
      Clock clock) {
    this.fifteenMinuteRollupDAO = fifteenMinuteRollupDAO;
    this.truckStopDAO = truckStopDAO;
    this.fifteenMinuteRollups = fifteenMinuteRollups;
    this.weeklyRollups = weeklyRollups;
    this.weeklyRollupDAO = weeklyRollupDAO;
    this.weeklyLocationStatsRollupDAO = weeklyLocationStatsRollupDAO;
    this.dailyRollupDAO = dailyRollupDAO;
    this.dailyRollups = dailyRollups;
    this.cache = cache;
    this.scheduleCounter = counter;
    this.clock = clock;
  }

  private TimeSeriesDAO dao(long interval, boolean location) {
    // TODO: this is a really stupid way to do it
    if (interval == 604800000L) {
      return location ? weeklyLocationStatsRollupDAO : weeklyRollupDAO;
    } else if (interval == DAY_IN_MILLIS) {
      return dailyRollupDAO;
    } else {
      return fifteenMinuteRollupDAO;
    }
  }

  private Slots slots(long interval) {
    if (interval == 604800000L) {
      return weeklyRollups;
    } else if (interval == DAY_IN_MILLIS) {
      return dailyRollups;
    } else {
      return fifteenMinuteRollups;
    }
  }

  @GET @Path("counts/{statList}") @Produces(MediaType.APPLICATION_JSON)
  public JResponse<List<StatVector>> getStatsFor(@PathParam("statList") final String statNames,
      @QueryParam("start") final long startTime, @QueryParam("end") final long endTime,
      @QueryParam("interval") final long interval, @QueryParam("nocache") boolean nocache) {
    String[] statList = statNames.split(",");
    Slots slots = slots(interval);
    boolean location = false;
    if (statList.length > 0 && statList[0].equals("trucksOnRoad")) {
      return trucksOnRoad(startTime, endTime, slots);
    } else if (statList.length > 0 && statList[0].contains("location")) {
      location = true;
    }
    if (statList.length  == 1 && !nocache) {
      List<StatVector> vectors = (List<StatVector>) cache.get(statList[0]);
      if (vectors != null) {
        log.log(Level.INFO, "Stats for {0} retrieved from cache", statList[0]);
        return JResponse.ok(vectors)
            .build();
      }
    }
    log.log(Level.INFO, "Requested stats: {0}", ImmutableList.copyOf(statList));
    List<SystemStats> stats = dao(interval, location).findWithinRange(startTime, endTime, statList);
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

  private JResponse<List<StatVector>> trucksOnRoad(long startTime, long endTime, Slots slots) {
    StatVector vector = slots.fillIn(ImmutableList.<SystemStats>of(), "trucksOnRoad", startTime, endTime);
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
