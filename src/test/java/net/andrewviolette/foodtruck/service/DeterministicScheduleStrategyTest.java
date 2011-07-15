package net.andrewviolette.foodtruck.service;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import net.andrewviolette.foodtruck.model.DayOfWeek;
import net.andrewviolette.foodtruck.model.Location;
import net.andrewviolette.foodtruck.model.ReoccurringTruckStop;
import net.andrewviolette.foodtruck.model.TimeRange;
import net.andrewviolette.foodtruck.model.Truck;
import net.andrewviolette.foodtruck.model.TruckStop;
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
    truck = new Truck("foo", "bar", null, null, "");
    stop1 =
        new ReoccurringTruckStop(truck, DayOfWeek.monday, new LocalTime(11, 0),
            new LocalTime(2, 0), new Location(1, 2, "Some location"));
    stop2 =
        new ReoccurringTruckStop(truck, DayOfWeek.monday, new LocalTime(15, 0), new LocalTime(17, 0),
            new Location(3, 4, "Another Location"));
    stop3 = new ReoccurringTruckStop(truck, DayOfWeek.thursday, new LocalTime(11, 0),
        new LocalTime(3, 4), new Location(1, 2, "Some location"));
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


