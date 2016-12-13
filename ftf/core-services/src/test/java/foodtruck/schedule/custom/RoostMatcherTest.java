package foodtruck.schedule.custom;

import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.Spot;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.custom.chicago.RoostMatcher;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 4/3/16
 */
public class RoostMatcherTest extends Mockito {
  @Mock private GeoLocator geoLocator;
  private DateTime tweetTime;
  private Truck truck;
  private RoostMatcher roostMatch;

  @Before
  public void setup() {
    tweetTime = new DateTime(2016, 1, 8, 7, 30, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder()
        .id("theroosttruck")
        .build();
    ImmutableList<Spot> commonSpots = ImmutableList.of(new Spot("600w", "600 West Chicago Avenue, Chicago, IL"));
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(new DateTime(1470237365629L));
    roostMatch = new RoostMatcher(geoLocator, commonSpots, DateTimeFormat.forStyle("SS"), clock);
  }

  @Test
  public void testHandle_allday() throws Exception {
    TruckStop stop = TruckStop.builder()
        .endTime(tweetTime.withTime(13, 0, 0, 0))
        .startTime(tweetTime.withTime(11, 0, 0, 0))
        .location(mock(Location.class))
        .truck(truck)
        .build();

    TruckStopMatch.Builder builder = TruckStopMatch.builder()
        .stop(stop);

    Story story = Story.builder()
        .text("Catch us at Clark & Monroe all day!")
        .time(tweetTime)
        .build();

    roostMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime);
  }

  @Test
  public void testHandle_notallday() throws Exception {
    TruckStop stop = TruckStop.builder()
        .endTime(tweetTime.withTime(13, 0, 0, 0))
        .startTime(tweetTime.withTime(11, 0, 0, 0))
        .location(mock(Location.class))
        .truck(truck)
        .build();

    TruckStopMatch.Builder builder = TruckStopMatch.builder()
        .stop(stop);

    Story story = Story.builder()
        .text("At Wacker and Adams for lunch! TREAT YO SELF")
        .time(tweetTime)
        .build();

    roostMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(stop.getStartTime());
  }
}