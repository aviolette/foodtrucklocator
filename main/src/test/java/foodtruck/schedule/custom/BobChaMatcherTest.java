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
import foodtruck.schedule.Spot;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.custom.chicago.BobChaMatcher;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;


/**
 * @author aviolette
 * @since 4/9/16
 */
@RunWith(MockitoJUnitRunner.class)
public class BobChaMatcherTest extends Mockito {
  @Mock private GeoLocator geoLocator;
  private BobChaMatcher matcher;
  private DateTime tweetTime;
  private Location loc1, loc2;
  private Truck truck;

  @Before
  public void setup() {
    truck = Truck.builder()
        .id("bobchafoodtruck")
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
    ImmutableList<Spot> commonSpots = ImmutableList.of(new Spot("600w", "600 West Chicago Avenue, Chicago, IL"),
        new Spot("wabash/vanburen", "Wabash and Van Buren, Chicago, IL"),
        new Spot("wacker/adams", "Wacker and Adams, Chicago, IL"),
        new Spot("clark/adams", "Clark and Adams, Chicago, IL"),
        new Spot("harrison/michigan", "Michigan and Harrison, Chicago, IL"),
        new Spot("lasalle/adams", "Lasalle and Adams, Chicago, IL"),
        new Spot("clark/monroe", "Clark and Monroe, Chicago, IL"),
        new Spot("wabash/jackson", "Wabash and Jackson, Chicago, IL"), new Spot("uchicago", "University of Chicago"),
        new Spot("uofc", "University of Chicago"), new Spot("58th/ellis", "University of Chicago"));
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(new DateTime(1470237365629L));
    matcher = new BobChaMatcher(geoLocator, commonSpots, DateTimeFormat.forStyle("SS"), clock);
  }

  @Test
  public void testHandleWeekend() {
    tweetTime = new DateTime(2016, 1, 9, 7, 30, 0, 0, DateTimeZone.UTC);
    Story story = Story.builder()
        .text("We will be open for business at U of C, and Clark & Monroe till 1:30pm!!")
        .time(tweetTime.withTime(11, 0, 0, 0))
        .userId("")
        .build();
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()).isNull();

  }

  @Test
  public void testHandle1() throws Exception {
    Story story = Story.builder()
        .text("We will be open for business at U of C, and Clark & Monroe till 1:30pm!!")
        .time(tweetTime.withTime(11, 0, 0, 0))
        .userId("")
        .build();
    geolocate("Clark and Monroe, Chicago, IL", loc1);
    geolocate("University of Chicago", loc2);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(1);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc2);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getTruck()).isEqualTo(truck);
  }

  @Test
  public void testHandle2() throws Exception {
    Story story = Story.builder()
        .text("Clark & Monroe! We are finally comin to you! Bob Cha II is waiting for you til 1:30 ;)\n" +
            "\n" +
            "Bob Cha I will be at U of C till 1:30 :)\n" +
            "See u soon!")
        .time(tweetTime.withTime(11, 0, 0, 0))
        .userId("")
        .build();
    geolocate("Clark and Monroe, Chicago, IL", loc1);
    geolocate("University of Chicago", loc2);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(1);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc2);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getTruck()).isEqualTo(truck);
  }

  private void geolocate(String address, Location location) {
    when(geoLocator.locate(address, GeolocationGranularity.NARROW)).thenReturn(location);
  }
}