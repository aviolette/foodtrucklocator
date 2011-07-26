package foodtruck.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.yaml.snakeyaml.Yaml;

import foodtruck.model.DayOfWeek;
import foodtruck.model.Location;
import foodtruck.model.ReoccurringTruckStop;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.ManualScheduleStrategy;
import foodtruck.schedule.ReoccuringScheduleStrategy;
import foodtruck.schedule.ScheduleStrategy;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class TruckConfigParserImpl implements TruckConfigParser {
  private static final Logger log = Logger.getLogger(TruckConfigParserImpl.class.getName());
  private final DateTimeZone zone;
  private final DateTimeFormatter formatter;

  @Inject
  public TruckConfigParserImpl(DateTimeZone localZone,
      @Named("configDate") DateTimeFormatter formatter) {
    zone = localZone;
    this.formatter = formatter;
  }
  // TODO: this is pretty atrocious code.  fix

  @Override
  public Map<Truck, ScheduleStrategy> parse(String url, ScheduleStrategy defaultStrategy)
      throws FileNotFoundException {
    Yaml yaml = new Yaml();
    Iterable<Map<String, Object>> trucks =
        (Iterable) yaml.loadAll(new BufferedReader(new FileReader(url)));
    ImmutableMap.Builder<Truck, ScheduleStrategy> truckBuilder = ImmutableMap.builder();
    for (Map<String, Object> truckMap : trucks) {
      Truck truck = new Truck.Builder()
          .id((String) truckMap.get("id"))
          .name((String) truckMap.get("name"))
          .iconUrl((String) truckMap.get("iconUrl"))
          .twitterHandle((String) truckMap.get("twitter"))
          .build();
      Map<String, Object> strategyObj = (Map) truckMap.get("strategy");
      ScheduleStrategy strategy = defaultStrategy;
      if (strategyObj != null) {
        String type = (String) strategyObj.get("type");
        if ("schedule".equals(type)) {
          strategy = scheduleStrategy(truck, strategyObj);
        } else if ("manual".equals(type)) {
          strategy = manualStrategy(truck, strategyObj);
        }
      }
      log.log(Level.INFO, "Loaded truck: {0}", truck);
      truckBuilder.put(truck, strategy);
    }
    return truckBuilder.build();
  }

  private ScheduleStrategy manualStrategy(Truck truck, Map<String, Object> strategyObj) {
    final ScheduleStrategy strategy;
    List<Map<String, Object>> scheduleList = (List) strategyObj.get("schedule");
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    for (Map<String, Object> scheduleData : scheduleList) {
      LocalTime startTime = getTime(scheduleData, "start");
      LocalTime endTime = getTime(scheduleData, "end");
      LocalDate date = getDate(scheduleData, "date");
      TruckStop stop =
          new TruckStop(truck,  date.toDateTime(startTime), date.toDateTime(endTime),
              new Location((Double) scheduleData.get("latitude"),
                  (Double) scheduleData.get("longitude"), (String) scheduleData.get("name")));
      stops.add(stop);
    }
    strategy = new ManualScheduleStrategy(stops.build());
    return strategy;
  }

  private LocalDate getDate(Map<String, Object> scheduleData, String key) {
    String timeValue = (String) scheduleData.get(key);
    return formatter.parseDateTime(timeValue).toLocalDate();
  }

  private ScheduleStrategy scheduleStrategy(Truck truck, Map<String, Object> strategyObj) {
    final ScheduleStrategy strategy;
    List<Map<String, Object>> scheduleList = (List) strategyObj.get("schedule");
    ImmutableList.Builder<ReoccurringTruckStop> stops = ImmutableList.builder();
    for (Map<String, Object> scheduleData : scheduleList) {
      LocalTime startTime = getTime(scheduleData, "start");
      LocalTime endTime = getTime(scheduleData, "end");
      ReoccurringTruckStop stop =
          new ReoccurringTruckStop(truck, DayOfWeek.valueOf((String) scheduleData.get("day")),
              startTime, endTime, new Location((Double) scheduleData.get("latitude"),
                  (Double) scheduleData.get("longitude"), (String) scheduleData.get("name")), zone);
      stops.add(stop);
    }
    strategy = new ReoccuringScheduleStrategy(stops.build());
    return strategy;
  }

  private LocalTime getTime(Map<String, Object> scheduleData, String key) {
    String timeValue = (String) scheduleData.get(key);
    String[] values = timeValue.split(":");
    return new LocalTime(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
  }
}
