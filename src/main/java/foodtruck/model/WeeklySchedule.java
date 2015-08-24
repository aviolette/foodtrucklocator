package foodtruck.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

/**
 * @author aviolette
 * @since 5/7/13
 */
public class WeeklySchedule {
  private final LocalDate start;
  private final Collection<LocationAndStops> stops;


  private WeeklySchedule(LocalDate start, Collection<LocationAndStops> stops) {
    this.start = start;
    this.stops = stops;
  }

  public LocalDate getStart() {
    return start;
  }

  public Collection<LocationAndStops> getStops() {
    return stops;
  }

  public WeeklySchedule sortFrom(Location center) {
    return new WeeklySchedule(start, new ByDistanceOrdering(center).immutableSortedCopy(stops));
  }

  public static class Builder {
    private Map<Location, LocationAndStops> map = Maps.newHashMap();
    private LocalDate start;

    public Builder() {
    }

    public Builder start(LocalDate start) {
      this.start = start;
      return this;
    }

    public Builder addStop(TruckStop stop) {
      if (!map.containsKey(stop.getLocation())) {
        map.put(stop.getLocation(), new LocationAndStops(
            start.toDateTimeAtStartOfDay(stop.getStartTime().getZone()), stop.getLocation()));
      }
      LocationAndStops row = map.get(stop.getLocation());
      row.addStop(stop);
      return this;
    }

    public WeeklySchedule build() {
      return new WeeklySchedule(start, map.values());
    }
  }

  private static class ByDistanceOrdering extends Ordering<LocationAndStops> {
    private Location center;

    public ByDistanceOrdering(Location center) {
      this.center = center;
    }

    @Override
    public int compare(@Nullable LocationAndStops left, @Nullable LocationAndStops right) {
      double ld = left.getLocation().distanceFrom(center),
          rd = right.getLocation().distanceFrom(center);
      if (ld > rd) {
        return 1;
      } else if (ld == rd) {
        return 0;
      } else {
        return -1;
      }
    }
  }

  public static class LocationAndStops {
    private final DateTime start;
    private final Location location;
    private List<Set<TruckStop>> stopsForWeek;

    public LocationAndStops(DateTime start, Location location) {
      stopsForWeek = Lists.newLinkedList();
      this.start = start;
      this.location = location;
      for (int i = 0; i < 7; i++) {
        stopsForWeek.add(Sets.<TruckStop>newHashSet());
      }
    }

    public Location getLocation() {
      return location;
    }

    public List<Set<TruckStop>> getStopsForWeek() {
      return stopsForWeek;
    }

    private void addStop(TruckStop stop) {
      DateTime time = stop.getStartTime().isBefore(start) ? stop.getEndTime() : stop.getStartTime();
      int dayOfWeek = (time.getDayOfWeek() == DateTimeConstants.SUNDAY) ? 0 : time.getDayOfWeek();
      Set<TruckStop> stops = stopsForWeek.get(dayOfWeek);
      stops.add(stop);
    }
  }
}
