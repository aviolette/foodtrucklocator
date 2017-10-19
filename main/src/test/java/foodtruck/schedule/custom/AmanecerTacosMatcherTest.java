package foodtruck.schedule.custom;

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
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.custom.chicago.AmanecerTacosMatcher;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 4/10/16
 */
@RunWith(MockitoJUnitRunner.class)
public class AmanecerTacosMatcherTest extends Mockito {
  @Mock private GeoLocator geoLocator;
  private AmanecerTacosMatcher matcher;
  private DateTime tweetTime;
  private Location loc1, loc2, loc3;
  private Truck truck;

  @Before
  public void setup() {
    truck = Truck.builder()
        .id("amanecertacos")
        .build();
    loc1 = Location.builder()
        .name("Location 1")
        .lat(1)
        .lng(1)
        .build();
    loc3 = Location.builder()
        .name("Location 2")
        .lat(2)
        .lng(2)
        .build();
    loc2 = Location.builder()
        .name("Location 3")
        .lat(1)
        .lng(1)
        .build();
    tweetTime = new DateTime(2016, 1, 11, 7, 30, 0, 0, DateTimeZone.UTC);
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(new DateTime(1470237365629L));
    matcher = new AmanecerTacosMatcher(geoLocator, DateTimeFormat.forStyle("SS"), clock);
  }

  @Test
  public void testHandle1() {
    Story story = Story.builder()
        .text("It's #FoodTruckFriday and this morning we are at:\n" +
            "8 Davis Station\n" +
            "9 Foster & Sheridan\n" +
            "10 Garrett & Sheridan\n" +
            "#Evanston #Northwestern ")
        .time(tweetTime)
        .build();

    geolocate("Davis Station", loc1);
    geolocate("Foster and Sheridan, Evanston, IL", loc2);
    geolocate("Garrett and Sheridan, Evanston, IL", loc3);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();
    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()).isNotNull();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(8, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(8, 45, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()).hasSize(2);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(9, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(9, 45, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc2);
    assertThat(match.getAdditionalStops()
        .get(1)
        .getStartTime()).isEqualTo(tweetTime.withTime(10, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getEndTime()).isEqualTo(tweetTime.withTime(10, 45, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getLocation()).isEqualTo(loc3);
  }

  @Test
  public void testHandle2() {
    Story story = Story.builder()
        .text("Today's #foodtruck schedule:\n" +
            "8a Foster & Sheridan\n" +
            "9a Noyes & Sheridan\n" +
            "10a Garrett & Sheridan\n" +
            "#Evanston #northwestern")
        .time(tweetTime)
        .build();

    geolocate("Sheridan and Noyes, Evanston, IL", loc2);
    geolocate("Foster and Sheridan, Evanston, IL", loc1);
    geolocate("Garrett and Sheridan, Evanston, IL", loc3);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();
    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()).isNotNull();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(8, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(8, 45, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()).hasSize(2);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(9, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(9, 45, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc2);
    assertThat(match.getAdditionalStops()
        .get(1)
        .getStartTime()).isEqualTo(tweetTime.withTime(10, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getEndTime()).isEqualTo(tweetTime.withTime(10, 45, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getLocation()).isEqualTo(loc3);
  }

  private void geolocate(String address, Location location) {
    when(geoLocator.locate(address, GeolocationGranularity.NARROW)).thenReturn(location);
  }
}