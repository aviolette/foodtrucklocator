package foodtruck.schedule;

import java.util.List;

import com.google.inject.internal.ImmutableList;

import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

/**
 * A strategy for determining stop times based on a one-time schedule
 * @author aviolette@gmail.com
 * @since Jul 21, 2011
 */
public class ManualScheduleStrategy implements ScheduleStrategy {
  private final List<TruckStop> stops;

  public ManualScheduleStrategy(List<TruckStop> stops) {
    this.stops = stops;
  }

  @Override
  public List<TruckStop> findForTime(Truck truck, TimeRange range) {
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    for (TruckStop stop : this.stops) {
      if (stop.getStartTime().isAfter(range.getStartDateTime())  ) {
        stops.add(stop);
      }
    }
    return stops.build();
  }
}
