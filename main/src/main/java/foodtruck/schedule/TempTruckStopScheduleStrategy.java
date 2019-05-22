package foodtruck.schedule;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.joda.time.Interval;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.StopOrigin;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.monitoring.CounterPublisher;
import foodtruck.time.Clock;
import foodtruck.time.TimeUtils;

import static foodtruck.time.TimeConversionUtils.toJoda;

/**
 * @author aviolette
 * @since 2018-12-10
 */
public class TempTruckStopScheduleStrategy implements ScheduleStrategy {

  private static final ImmutableList<String> CALENDARS = ImmutableList.of("pollyanna", "werkforce", "scorchedearth",
      "Google: Hickory Creek Brewing Company", "Royal Palms", "Alter Brewing", "squarespace: Plank Road Tap Room",
      "squarespace: Plank Road Tap Room", "Google: Oswego Brewing Co.",
      "Google: p8uim1tjcpuejjcnmchidh5p1hghvbmc@import.calendar.google.com", "ical: bigwangschicago",
      "ical: bigwangschicago", "imperialoak", "skeletonkey", "thefatshallot", "squarespace: Temperance Beer Co.");
  private final TempTruckStopDAO tempTruckStopDAO;
  private final LocationDAO locationDAO;
  private final TruckDAO truckDAO;
  private final Clock clock;
  private final CounterPublisher publisher;

  @Inject
  public TempTruckStopScheduleStrategy(TempTruckStopDAO tempTruckStopDAO, LocationDAO locationDAO, TruckDAO truckDAO,
      Clock clock, CounterPublisher publisher) {
    this.tempTruckStopDAO = tempTruckStopDAO;
    this.locationDAO = locationDAO;
    this.truckDAO = truckDAO;
    this.clock = clock;
    this.publisher = publisher;
  }

  @Override
  public List<TruckStop> findForTime(Interval range, @Nullable Truck searchTruck) {
    String now = clock.nowFormattedAsTime();
    Collection<TempTruckStop> tempTruckStops = tempTruckStopDAO.findDuring(range, searchTruck)
        .stream()
        .distinct()
        .sorted((ts1, ts2) -> {
          if (ts1.getTruckId().equals(ts2.getTruckId())) {
            if (ts2.getLocationName().equals(ts1.getStartTime())) {
              if (ts1.getStartTime().equals(ts2.getStartTime())) {
                return ts1.getEndTime().compareTo(ts2.getEndTime());
              }
              return ts1.getStartTime().compareTo(ts2.getStartTime());
            } else {
              return ts1.getLocationName().compareTo(ts2.getLocationName());
            }
          } else {
            return ts1.getTruckId().compareTo(ts2.getTruckId());
          }
        })
        .collect(Collectors.toList());

    List<TempTruckStop> items = new LinkedList<>();
    TempTruckStop last = null;
    for (TempTruckStop stop : tempTruckStops) {
      if (last == null || !stop.getTruckId()
            .equals(last.getTruckId()) || !stop.getLocationName()
            .equals(last.getLocationName()) ||
            !TimeUtils.overlapsOrContains(stop.getStartTime(), stop.getEndTime(), last.getStartTime(),
                last.getEndTime())) {
          items.add(stop);

      }
      last = stop;
    }
    tempTruckStops = items;
    publishCountStatistics(tempTruckStops);

    List<TruckStop> stops =
        tempTruckStops.stream()
        .filter(temp -> locationDAO.findByAliasOpt(temp.getLocationName()).isPresent())
        .map(temp -> TruckStop.builder()
            .endTime(toJoda(temp.getEndTime()))
            .startTime(toJoda(temp.getStartTime()))
            .location(locationDAO.findByAliasOpt(temp.getLocationName())
                .orElseThrow(() -> new RuntimeException("Location not found: " + temp.getLocationName())))
            .truck(truckDAO.findByIdOpt(temp.getTruckId())
                .orElseThrow(() -> new RuntimeException("Truck not found: " + temp.getTruckId())))
            .appendNote("From calendar: " + temp.getCalendarName() + " @ " + now)
            .origin(StopOrigin.VENDORCAL)
            .build())
        .collect(Collectors.toList());
    return stops;
  }

  private void publishCountStatistics(Collection<TempTruckStop> tempTruckStops) {
    Map<String, Integer> counts = new HashMap<>();
    CALENDARS.forEach(cal -> counts.put(cal, 0));
    for (TempTruckStop tempTruckStop : tempTruckStops) {
      counts.merge(tempTruckStop.getCalendarName(), 1, Integer::sum);
    }
    long nowMillis = clock.nowInMillis();
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      publisher.increment("calendar_contribution", entry.getValue(), nowMillis, ImmutableMap.of("calendar", entry.getKey()));
    }
  }
}
