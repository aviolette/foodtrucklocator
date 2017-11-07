package foodtruck.schedule;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;

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
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.ModelTestHelper.clarkAndMonroe;
import static foodtruck.schedule.ModelTestHelper.truck1;
import static foodtruck.schedule.ModelTestHelper.truck2;
import static foodtruck.schedule.ModelTestHelper.truck3;
import static foodtruck.schedule.ModelTestHelper.truck4;
import static foodtruck.schedule.ModelTestHelper.wackerAndAdams;
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

  @Before
  public void setup() {
    service = new FoodTruckStopServiceImpl(truckStopDAO, schedule, clock, truckDAO, locationDAO,
        messageDAO, dailyDataDAO);
  }

  @Test
  public void findTrucksNearLocation() {
    Location location = clarkAndMonroe();
    DateTime dt = new DateTime(2017, 11, 6, 12, 0);
    DateTime startTime = new DateTime(2017, 11, 6, 11, 0);
    DateTime endTime = new DateTime(2017, 11, 6, 14, 0);
    when(locationDAO.findByName(location.getName())).thenReturn(Optional.of(location));
    TruckStop stop1 = TruckStop.builder()
        .truck(truck1())
        .endTime(endTime)
        .startTime(startTime)
        .location(clarkAndMonroe())
        .build(),
    stop2 = TruckStop.builder()
        .truck(truck2())
        .endTime(endTime)
        .startTime(startTime)
        .location(clarkAndMonroe())
        .build(),
    stop3 = TruckStop.builder()
        .startTime(startTime)
        .endTime(endTime)
        .truck(truck3())
        .location(wackerAndAdams())
        .build(),
    stop4 = TruckStop.builder()
        .startTime(startTime.minusHours(4))
        .endTime(startTime.minusHours(2))
        .location(clarkAndMonroe())
        .truck(truck4())
        .build();
    List<TruckStop> truckStops = ImmutableList.of(stop1, stop2, stop3, stop4);
    when(truckStopDAO.findDuring(null, dt.toLocalDate())).thenReturn(truckStops);
    Set<Truck> trucks = service.findTrucksNearLocation(location, dt);
    assertThat(trucks).containsExactly(truck1(), truck2());
  }
}