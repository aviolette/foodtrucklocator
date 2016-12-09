package foodtruck.jersey;

import java.util.logging.Logger;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.LocalDate;

import foodtruck.caching.Cacher;
import foodtruck.model.DailySchedule;
import foodtruck.model.StaticConfig;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.schedule.ScheduleCacher;
import foodtruck.server.resources.json.DailyScheduleWriter;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 12/8/16
 */
public class ScheduleCacherImpl implements ScheduleCacher {
  private static final Logger log = Logger.getLogger(ScheduleCacherImpl.class.getName());

  private static final String DAILY_SCHEDULE = "daily_schedule";
  private static final String TOMORROWS_SCHEDULE = "tomorrows_schedule";
  private final Clock clock;
  private final StaticConfig staticConfig;
  private Cacher cacher;
  private DailyScheduleWriter writer;
  private FoodTruckStopService service;

  @Inject
  public ScheduleCacherImpl(Cacher cacher, DailyScheduleWriter writer, FoodTruckStopService service, Clock clock,
      StaticConfig staticConfig) {
    this.cacher = cacher;
    this.writer = writer;
    this.service = service;
    this.clock = clock;
    this.staticConfig = staticConfig;
  }

  @Override
  public String findSchedule() {
    return fetchSchedule(clock.currentDay(), DAILY_SCHEDULE);
  }

  private String fetchSchedule(LocalDate localDate, String key) {
    if (staticConfig.isScheduleCachingOn() && cacher.contains(key)) {
      log.info("Schedule loaded from cache");
      return (String) cacher.get(key);
    }
    DailySchedule schedule = service.findStopsForDay(localDate);
    log.info("Schedule loaded from db");
    try {
      JSONObject json = writer.asJSON(schedule);
      String payload = json.toString();
      if (staticConfig.isScheduleCachingOn()) {
        cacher.put(key, payload, 5);
      }
      return payload;
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void invalidate() {
    cacher.delete(DAILY_SCHEDULE);
    cacher.delete(TOMORROWS_SCHEDULE);
  }

  @Override
  public String findTomorrowsSchedule() {
    return fetchSchedule(clock.currentDay()
        .plusDays(1), TOMORROWS_SCHEDULE);
  }
}
