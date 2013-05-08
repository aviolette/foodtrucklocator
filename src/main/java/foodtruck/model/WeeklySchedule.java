package foodtruck.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

/**
 * @author aviolette
 * @since 5/7/13
 */
public class WeeklySchedule {
  private LocalDate start;
  private Collection<LocationAndStops> stops;

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
            start.toDateMidnight(stop.getStartTime().getZone()).toDateTime(), stop.getLocation()));
      }
      LocationAndStops row = map.get(stop.getLocation());
      row.addStop(stop);
      return this;
    }

    public WeeklySchedule build() {
      return new WeeklySchedule(start, map.values());
    }
  };

  public static class LocationAndStops {
    private final DateTime start;
    private final Location location;
    private List<Set<TruckStop>> stopsForWeek;

    public LocationAndStops(DateTime start, Location location) {
      stopsForWeek = Lists.newLinkedList();
      this.start = start;
      this.location = location;
      for (int i=0; i < 7; i++) {
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
