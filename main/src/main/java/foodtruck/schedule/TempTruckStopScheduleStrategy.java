package foodtruck.schedule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

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

import static foodtruck.time.TimeConversionUtils.toJoda;

/**
 * @author aviolette
 * @since 2018-12-10
 */
public class TempTruckStopScheduleStrategy implements ScheduleStrategy {

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
    List<TempTruckStop> tempTruckStops = tempTruckStopDAO.findDuring(range, searchTruck)
        .stream()
        .distinct()
        .collect(Collectors.toList());

    Map<String, Integer> counts = new HashMap<>();
    for (TempTruckStop tempTruckStop : tempTruckStops) {
      counts.merge(tempTruckStop.getCalendarName(), 1, Integer::sum);
    }
    long nowMillis = clock.nowInMillis();
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      publisher.increment("calendar_contribution", entry.getValue(), nowMillis, ImmutableMap.of("calendar", entry.getKey()));
    }
    List<TruckStop> stops =
        tempTruckStops.stream()
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
}
