package foodtruck.model;

import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

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
  public void prependNotes() {
    stop = TruckStop.builder(stop).appendNote("hello world").build();
    assertThat(stop.getNotes()).contains("hello world");
    stop = TruckStop.builder(stop).prependNotes(ImmutableList.of("goodbye1", "goodbye2")).build();
    assertThat(stop.getNotes()).containsAllOf("goodbye1", "goodbye2", "hello world");
  }

  @Test
  public void prependNotesWithNoExistingNotes() {
    stop = TruckStop.builder(stop).prependNotes(ImmutableList.of("goodbye1", "goodbye2")).build();
    assertThat(stop.getNotes()).containsExactly("goodbye1", "goodbye2");
  }

  private TruckStop createTruckStop(DateTime startTime, DateTime endTime) {
    Truck truck = new Truck.Builder().name("foo").id("bar").build();
    Location location = Location.builder().lat(-1.0d).lng(-2.0d).build();
    return TruckStop.builder().truck(truck).startTime(startTime).endTime(endTime).location(location).build();
  }
}
