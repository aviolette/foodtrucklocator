package foodtruck.schedule;

import java.util.List;

import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

/**
 * An interface for determining a truck's schedule.
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
public interface ScheduleStrategy {
  /**
   * Finds all the truck stops for a truck over a time period on a day.
   * @return the list of stops sorted by time
   */
  public List<TruckStop> findForTime(TimeRange range, Truck searchTruck);
}
