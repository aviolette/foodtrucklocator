package foodtruck.schedule;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.internal.ImmutableList;

import foodtruck.model.ReoccurringTruckStop;
import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

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

  @VisibleForTesting public List<ReoccurringTruckStop> getStops() {
    return stops;
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
