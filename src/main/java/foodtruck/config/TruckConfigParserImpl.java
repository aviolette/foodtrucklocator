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

import org.joda.time.LocalTime;
import org.yaml.snakeyaml.Yaml;

import foodtruck.schedule.DeterministicScheduleStrategy;
import foodtruck.model.DayOfWeek;
import foodtruck.model.Location;
import foodtruck.model.ReoccurringTruckStop;
import foodtruck.model.Truck;
import foodtruck.schedule.ScheduleStrategy;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class TruckConfigParserImpl implements TruckConfigParser {
  private static final Logger log = Logger.getLogger(TruckConfigParserImpl.class.getName());

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
          List<Map<String, Object>> scheduleList = (List) strategyObj.get("schedule");
          ImmutableList.Builder<ReoccurringTruckStop> stops = ImmutableList.builder();
          for (Map<String, Object> scheduleData : scheduleList) {
            LocalTime startTime = getTime(scheduleData, "start");
            LocalTime endTime = getTime(scheduleData, "end");
            ReoccurringTruckStop stop =
                new ReoccurringTruckStop(truck, DayOfWeek.valueOf((String) scheduleData.get("day")),
                    startTime, endTime, new Location((Double) scheduleData.get("latitude"),
                        (Double) scheduleData.get("longitude"), (String) scheduleData.get("name")));
            stops.add(stop);
          }
          strategy = new DeterministicScheduleStrategy(stops.build());
        }
      }
      log.log(Level.INFO, "Loaded truck: {0}", truck);
      truckBuilder.put(truck, strategy);
    }
    return truckBuilder.build();
  }


  private LocalTime getTime(Map<String, Object> scheduleData, String key) {
    String timeValue = (String) scheduleData.get(key);
    String[] values = timeValue.split(":");
    return new LocalTime(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
  }

}
