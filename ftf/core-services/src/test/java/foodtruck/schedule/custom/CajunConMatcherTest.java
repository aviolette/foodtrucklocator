package foodtruck.schedule.custom;

import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.Spot;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.custom.chicago.CajunConMatcher;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 5/11/16
 */
@RunWith(MockitoJUnitRunner.class)
public class CajunConMatcherTest extends Mockito {
  private Truck truck;
  private Location loc1;
  private Location loc2;
  @Mock private GeoLocator geoLocator;
  private DateTime tweetTime;
  private CajunConMatcher matcher;

  @Before
  public void setup() {
    truck = Truck.builder()
        .id("thecajuncon")
        .build();
    loc1 = Location.builder()
        .name("Location 1")
        .lat(1)
        .lng(1)
        .build();
    loc2 = Location.builder()
        .name("Location 3")
        .lat(1)
        .lng(1)
        .build();
    tweetTime = new DateTime(2016, 1, 11, 7, 30, 0, 0, DateTimeZone.UTC);
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(new DateTime(1470237365629L));
    matcher = new CajunConMatcher(geoLocator, ImmutableList.<Spot>of(), DateTimeFormat.forStyle("SS"), clock);
  }

  @Test
  public void testHandle() {
    Story story = Story.builder()
        .text("Happy WEDNESDAY TCC family. \n" +
            "\n" +
            "27th California: 6am-830am\n" +
            "11th Hamilton: 1030am-130p\n" +
            "@chifoodtruckz @TrucksforLunch \n" +
            "\n" +
            "#CookCountyWed")
        .time(tweetTime.withTime(5, 0, 0, 0))
        .userId("")
        .build();
    TruckStopMatch.Builder builder = TruckStopMatch.builder();
    builder.stop(TruckStop.builder()
        .location(loc1)
        .truck(truck)
        .endTime(tweetTime.withTime(8, 30, 0, 0))
        .startTime(tweetTime.withTime(6, 0, 0, 0))
        .build());
    geolocate("1100 South Hamilton, Chicago, IL", loc2);

    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(6, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(8, 30, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()).hasSize(1);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(10, 30, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(13, 30, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc2);
  }

  private void geolocate(String address, Location location) {
    when(geoLocator.locate(address, GeolocationGranularity.NARROW)).thenReturn(location);
  }
}