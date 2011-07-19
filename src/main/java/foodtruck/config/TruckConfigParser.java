package foodtruck.config;

import java.io.FileNotFoundException;
import java.util.Map;

import foodtruck.schedule.ScheduleStrategy;
import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public interface TruckConfigParser {
  Map<Truck, ScheduleStrategy> parse(String url, ScheduleStrategy strategy)
      throws FileNotFoundException;
}
