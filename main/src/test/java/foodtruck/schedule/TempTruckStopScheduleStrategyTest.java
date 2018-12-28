package foodtruck.schedule;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.model.TruckStop;
import foodtruck.time.Clock;
import foodtruck.util.FakeClock;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.ModelTestHelper.clarkAndMonroe;
import static foodtruck.schedule.ModelTestHelper.truck1;
import static foodtruck.schedule.ModelTestHelper.truck2;
import static foodtruck.schedule.ModelTestHelper.truck3;
import static foodtruck.schedule.ModelTestHelper.wackerAndAdams;

/**
 * @author aviolette
 * @since 2018-12-20
 */
@RunWith(MockitoJUnitRunner.class)
public class TempTruckStopScheduleStrategyTest extends Mockito {

  private TempTruckStopScheduleStrategy strategy;
  @Mock private TempTruckStopDAO tempDAO;
  @Mock private LocationDAO locationDAO;
  @Mock private TruckDAO truckDAO;
  private Clock clock;

  @Before
  public void before() {
    clock = FakeClock.fixed(1545320974000L);
    strategy = new TempTruckStopScheduleStrategy(tempDAO, locationDAO, truckDAO, clock);
  }


  @Test
  public void findForTime() {
    TempTruckStop.Builder builder = TempTruckStop.builder()
        .calendarName("foo")
        .locationName(wackerAndAdams().getName())
        .startTime(ZonedDateTime.of(2018, 12, 20, 10, 30, 0, 0, clock.zone8()))
        .endTime(ZonedDateTime.of(2018, 12, 20, 14, 30, 0, 0, clock.zone8()))
        .truckId(truck1().getId());
    TempTruckStop stop1 = builder.build();
    TempTruckStop stop2 = builder.endTime(ZonedDateTime.of(2018, 12, 20, 14, 40, 0, 0, clock.zone8()))
        .build();
    TempTruckStop stop3 = builder.locationName(clarkAndMonroe().getName()).truckId(truck2().getId()).build();
    TempTruckStop stop4 = builder.locationName(clarkAndMonroe().getName()).truckId(truck3().getId()).build();

    Interval interval = new Interval(1545151584000L, 1546015584000L);

    when(tempDAO.findDuring(interval, null)).thenReturn(ImmutableList.of(stop1, stop2, stop3, stop4));
    when(locationDAO.findByAliasOpt(wackerAndAdams().getName())).thenReturn(Optional.of(wackerAndAdams()));
    when(locationDAO.findByAliasOpt(clarkAndMonroe().getName())).thenReturn(Optional.of(clarkAndMonroe()));
    when(truckDAO.findByIdOpt(truck1().getId())).thenReturn(Optional.of(truck1()));
    when(truckDAO.findByIdOpt(truck2().getId())).thenReturn(Optional.of(truck2()));
    when(truckDAO.findByIdOpt(truck3().getId())).thenReturn(Optional.of(truck3()));
    List<TruckStop> stops = strategy.findForTime(interval, null);
    assertThat(stops).hasSize(3);
  }
}