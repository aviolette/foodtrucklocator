package foodtruck.schedule;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.MessageDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.model.TruckStopWithCounts;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.ModelTestHelper.clarkAndMonroe;
import static foodtruck.schedule.ModelTestHelper.truck1;
import static foodtruck.schedule.ModelTestHelper.truck2;
import static foodtruck.schedule.ModelTestHelper.truck3;
import static foodtruck.schedule.ModelTestHelper.truck4;
import static foodtruck.schedule.ModelTestHelper.wackerAndAdams;
import static foodtruck.schedule.ScheduleModule.GOOGLE_CALENDAR;
import static org.mockito.Mockito.when;

/**
 * @author aviolette
 * @since 11/6/17
 */
@RunWith(MockitoJUnitRunner.class)
public class FoodTruckStopServiceImplTest {

  private @Mock TruckStopDAO truckStopDAO;
  private @Mock ScheduleStrategy schedule;
  private @Mock Clock clock;
  private @Mock TruckDAO truckDAO;
  private @Mock LocationDAO locationDAO;
  private @Mock MessageDAO messageDAO;
  private @Mock DailyDataDAO dailyDataDAO;
  private FoodTruckStopServiceImpl service;
  private final DateTime lunchStart = new DateTime(2017, 11, 6, 11, 0);
  private final DateTime lunchEnd = new DateTime(2017, 11, 6, 14, 0);
  @Before
  public void setup() {
    service = new FoodTruckStopServiceImpl(truckStopDAO, ImmutableMap.of(GOOGLE_CALENDAR, schedule), clock, truckDAO, locationDAO,
        messageDAO, dailyDataDAO);
  }

  @Test
  public void findStopsForTruckAfter() {
    TruckStop stop1 = TruckStop.builder()
        .truck(truck1())
        .endTime(lunchEnd)
        .startTime(lunchStart)
        .location(clarkAndMonroe())
        .build();
    TruckStop stop2 = TruckStop.builder()
        .truck(truck1())
        .startTime(lunchStart.plusHours(7))
        .endTime(lunchEnd.plusHours(7))
        .location(clarkAndMonroe())
        .build();
    TruckStop stop3 = TruckStop.builder()
        .startTime(lunchStart)
        .endTime(lunchEnd)
        .truck(truck2())
        .location(wackerAndAdams())
        .build();
    TruckStop stop4 = TruckStop.builder()
        .startTime(lunchStart)
        .endTime(lunchEnd)
        .location(clarkAndMonroe())
        .truck(truck4())
        .build();
    final DateTime dt = lunchStart.minusHours(1);
    List<TruckStop> truckStops = ImmutableList.of(stop1, stop2, stop3, stop4);
    when(truckStopDAO.findAfter(dt)).thenReturn(truckStops);
    List<TruckStopWithCounts> stops = service.findStopsForTruckAfter(truck1().getId(), dt);
    assertThat(stops).hasSize(2);
  }

  @Test
  public void findTrucksNearLocation() {
    Location location = clarkAndMonroe();
    DateTime dt = new DateTime(2017, 11, 6, 12, 0);
    DateTime endTime = new DateTime(2017, 11, 6, 14, 0);
    when(locationDAO.findByName(location.getName())).thenReturn(Optional.of(location));
    TruckStop stop1 = TruckStop.builder()
        .truck(truck1())
        .endTime(endTime)
        .startTime(lunchStart)
        .location(clarkAndMonroe())
        .build(),
    stop2 = TruckStop.builder()
        .truck(truck2())
        .endTime(endTime)
        .startTime(lunchStart)
        .location(clarkAndMonroe())
        .build(),
    stop3 = TruckStop.builder()
        .startTime(lunchStart)
        .endTime(endTime)
        .truck(truck3())
        .location(wackerAndAdams())
        .build(),
    stop4 = TruckStop.builder()
        .startTime(lunchStart.minusHours(4))
        .endTime(lunchStart.minusHours(2))
        .location(clarkAndMonroe())
        .truck(truck4())
        .build();
    List<TruckStop> truckStops = ImmutableList.of(stop1, stop2, stop3, stop4);
    when(truckStopDAO.findDuring(null, dt.toLocalDate())).thenReturn(truckStops);
    Set<Truck> trucks = service.findTrucksNearLocation(location, dt);
    assertThat(trucks).containsExactly(truck1(), truck2());
  }
}