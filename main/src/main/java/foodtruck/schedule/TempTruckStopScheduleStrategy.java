package foodtruck.schedule;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import org.joda.time.Interval;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
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

  @Inject
  public TempTruckStopScheduleStrategy(TempTruckStopDAO tempTruckStopDAO, LocationDAO locationDAO, TruckDAO truckDAO,
      Clock clock) {
    this.tempTruckStopDAO = tempTruckStopDAO;
    this.locationDAO = locationDAO;
    this.truckDAO = truckDAO;
    this.clock = clock;
  }

  @Override
  public List<TruckStop> findForTime(Interval range, @Nullable Truck searchTruck) {
    String now = clock.nowFormattedAsTime();
    List<TruckStop> stops = tempTruckStopDAO.findDuring(range, searchTruck)
        .stream()
        .distinct()
        .map(temp -> TruckStop.builder()
            .endTime(toJoda(temp.getEndTime()))
            .startTime(toJoda(temp.getStartTime()))
            .location(locationDAO.findByName(temp.getLocationName())
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
