package foodtruck.truckstops;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import foodtruck.schedule.DeterministicScheduleStrategy;
import foodtruck.model.DayOfWeek;
import foodtruck.model.Location;
import foodtruck.model.ReoccurringTruckStop;
import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author aviolette@gmail.com
 * @since Jul 15, 2011
 */
public class DeterministicScheduleStrategyTest {
  private DeterministicScheduleStrategy strategy;
  private Truck truck;
  private List<ReoccurringTruckStop> stops;
  private ReoccurringTruckStop stop1;
  private ReoccurringTruckStop stop2;
  private ReoccurringTruckStop stop3;

  @Before
  public void before() {
    DateTimeZone zone = DateTimeZone.forID("America/Chicago");
    truck = new Truck.Builder().id("foo").name("bar").build();
    stop1 =
        new ReoccurringTruckStop(truck, DayOfWeek.monday, new LocalTime(11, 0),
            new LocalTime(2, 0), new Location(1, 2, "Some location"), zone);
    stop2 =
        new ReoccurringTruckStop(truck, DayOfWeek.monday, new LocalTime(15, 0), new LocalTime(17, 0),
            new Location(3, 4, "Another Location"), zone);
    stop3 = new ReoccurringTruckStop(truck, DayOfWeek.thursday, new LocalTime(11, 0),
        new LocalTime(3, 4), new Location(1, 2, "Some location"), zone);
    stops = ImmutableList.of(stop1, stop2, stop3);
    strategy = new DeterministicScheduleStrategy(stops);
  }

  @Test
  public void testNoStopsOnUnspecifiedDay() {
    LocalDate friday = new LocalDate(2011, 7, 15);
    List<TruckStop> outcome = strategy
        .findForTime(truck, new TimeRange(friday, new LocalTime(11, 0), new LocalTime(23, 59)));
    assertTrue(outcome.isEmpty());
  }

  @Test
  public void testFindsValuesInFullDayRange() {
    LocalDate monday = new LocalDate(2011, 7, 11);
    List<TruckStop> outcome = strategy
        .findForTime(truck, new TimeRange(monday, new LocalTime(11, 0), new LocalTime(23, 59)));
    assertEquals(2, outcome.size());
    assertEquals(ImmutableList.of(stop1.toTruckStop(monday), stop2.toTruckStop(monday)), outcome);
  }

  @Test
  public void testFindsValuesInPartialDayRange() {
    LocalDate monday = new LocalDate(2011, 7, 11);
    List<TruckStop> outcome = strategy
        .findForTime(truck, new TimeRange(monday, new LocalTime(11, 0), new LocalTime(2, 30)));
    assertEquals(ImmutableList.of(stop1.toTruckStop(monday)), outcome);
  }
}


