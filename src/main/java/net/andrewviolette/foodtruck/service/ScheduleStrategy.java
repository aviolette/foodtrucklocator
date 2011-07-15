package net.andrewviolette.foodtruck.service;

import java.util.List;

import net.andrewviolette.foodtruck.model.TimeRange;
import net.andrewviolette.foodtruck.model.Truck;
import net.andrewviolette.foodtruck.model.TruckStop;

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
  public List<TruckStop> findForTime(Truck truck, TimeRange range);
}
