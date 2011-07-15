package net.andrewviolette.foodtruck.service;

import java.util.List;

import com.google.inject.internal.ImmutableList;

import net.andrewviolette.foodtruck.model.ReoccurringTruckStop;
import net.andrewviolette.foodtruck.model.TimeRange;
import net.andrewviolette.foodtruck.model.Truck;
import net.andrewviolette.foodtruck.model.TruckStop;
import net.andrewviolette.foodtruck.service.ScheduleStrategy;

/**
 * A strategy for determining a truck's location based on a regularly-occurring set of stops.
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
public class DeterministicScheduleStrategy implements ScheduleStrategy {
  private final List<ReoccurringTruckStop> stops;

  public DeterministicScheduleStrategy(List<ReoccurringTruckStop> stops) {
    this.stops = stops;
  }

  @Override
  public List<TruckStop> findForTime(Truck truck, TimeRange range) {
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    for (ReoccurringTruckStop stop : this.stops) {
      if (stop.in(range)) {
        stops.add(stop.toTruckStop(range.getDate()));
      }
    }
    return stops.build();
  }
}
