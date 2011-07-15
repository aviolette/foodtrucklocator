package net.andrewviolette.foodtruck.service;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.andrewviolette.foodtruck.model.TimeRange;
import net.andrewviolette.foodtruck.model.Truck;
import net.andrewviolette.foodtruck.model.TruckStop;
import net.andrewviolette.foodtruck.service.ScheduleStrategy;

/**
 * A strategy to pull time and location data out of a food truck's twitter feed.
 * 
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
public class TwitterFeedScheduleStrategy implements ScheduleStrategy {
  @Override
  public List<TruckStop> findForTime(Truck truck, TimeRange range) {
    return ImmutableList.of();
  }
}
