package foodtruck.model;

import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class TruckStopTest {
  private TruckStop stop;

  @Before
  public void before() {
    stop = createTruckStop(new DateTime(2011, 7, 11, 11, 0, 0, 0, DateTimeZone.UTC),
        new DateTime(2011, 7, 11, 12, 0, 0, 0, DateTimeZone.UTC));
  }

  @Test
  public void testWithinEarlierDay() {
    assertFalse(stop.within(new TimeRange(new LocalDate(2011, 6, 11), DateTimeZone.UTC)));
  }

  @Test
  public void testWithinFullDay() {
    assertTrue(stop.within(new TimeRange(new LocalDate(2011, 7, 11), DateTimeZone.UTC)));
  }

  @Test
  public void testWithBeforeStart() {
    assertFalse(stop.within(new TimeRange(new LocalDate(2011, 7, 11),
        new LocalTime(10, 0), new LocalTime(10, 59))));
  }

  @Test
  public void testWithinDayAtEnd() {
    // exclusive at upper bound
    assertFalse(stop.within(new TimeRange(new LocalDate(2011, 7, 11),
        new LocalTime(10, 0), new LocalTime(11, 0))));
  }

  @Test
  public void testWithinLower() {
    // inclusive at lower bound
    assertTrue(stop.within(new TimeRange(new LocalDate(2011, 7, 11),
        new LocalTime(11, 0), new LocalTime(12, 0))));
  }

  @Test
  public void testWithinDayLaterDay() {
    assertFalse(stop.within(new TimeRange(new LocalDate(2011, 8, 11), DateTimeZone.UTC)));
  }

  @Test
  public void prependNotes() {
    stop = TruckStop.builder(stop).appendNote("hello world").build();
    assertThat(stop.getNotes(), hasItem("hello world"));
    stop = TruckStop.builder(stop).prependNotes(ImmutableList.of("goodbye1", "goodbye2")).build();
    assertEquals(ImmutableList.of("goodbye1", "goodbye2", "hello world"), stop.getNotes());
  }

  @Test
  public void prependNotesWithNoExistingNotes() {
    stop = TruckStop.builder(stop).prependNotes(ImmutableList.of("goodbye1", "goodbye2")).build();
    assertEquals(ImmutableList.of("goodbye1", "goodbye2"), stop.getNotes());
  }

  private TruckStop createTruckStop(DateTime startTime, DateTime endTime) {
    Truck truck = new Truck.Builder().name("foo").id("bar").build();
    Location location = Location.builder().lat(-1.0d).lng(-2.0d).build();
    return TruckStop.builder().truck(truck).startTime(startTime).endTime(endTime).location(location).build();
  }
}
