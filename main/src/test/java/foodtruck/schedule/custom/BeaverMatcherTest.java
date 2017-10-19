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
import foodtruck.schedule.custom.chicago.BeaverMatcher;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 3/29/16
 */
@RunWith(MockitoJUnitRunner.class)
public class BeaverMatcherTest extends Mockito {
  @Mock private GeoLocator geoLocator;
  private BeaverMatcher beaverMatch;
  private Truck truck;
  private DateTime tweetTime;
  private Location loc1, loc2, loc3;
  private Clock clock;

  @Before
  public void setup() {
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
    tweetTime = new DateTime(2016, 1, 8, 7, 30, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder()
        .id("beaversdonuts")
        .build();
    ImmutableList<Spot> commonSpots = ImmutableList.of(new Spot("600w", "600 West Chicago Avenue, Chicago, IL"),
        new Spot("wabash/vanburen", "Wabash and Van Buren, Chicago, IL"),
        new Spot("wacker/adams", "Wacker and Adams, Chicago, IL"),
        new Spot("clark/adams", "Clark and Adams, Chicago, IL"),
        new Spot("clark/jackson", "Clark and Jackson, Chicago, IL"),
        new Spot("harrison/michigan", "Michigan and Harrison, Chicago, IL"),
        new Spot("lasalle/adams", "Lasalle and Adams, Chicago, IL"),
        new Spot("clark/monroe", "Clark and Monroe, Chicago, IL"),
        new Spot("wabash/jackson", "Wabash and Jackson, Chicago, IL"), new Spot("uchicago", "University of Chicago"),
        new Spot("58th/ellis", "University of Chicago"));
    clock = mock(Clock.class);
    when(clock.now()).thenReturn(new DateTime(1470237365629L));
    beaverMatch = new BeaverMatcher(geoLocator, commonSpots, DateTimeFormat.forStyle("SS"), clock);
  }

  @Test
  public void test1() {
    Story story = Story.builder()
        .text("\n" +
            "Open This Morning\n" +
            "@600WestBuilding\n" +
            "Wabash & Van Buren\n" +
            "Wacker & Adams\n" +
            "And Inside @ChiFrenchMarket\n" +
            "#HumpDayDonuts ")
        .time(tweetTime)
        .build();

    geolocate("Wacker and Adams, Chicago, IL", loc1);
    geolocate("600 West Chicago Avenue, Chicago, IL", loc2);
    geolocate("Wabash and Van Buren, Chicago, IL", loc3);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    beaverMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc2);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(2);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc3);
    assertThat(match.getAdditionalStops()
        .get(1)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getLocation()).isEqualTo(loc1);

  }

  @Test
  public void test2() {
    tweetTime = new DateTime(2016, 1, 8, 7, 30, 0, 0, DateTimeZone.UTC);
    Story story = Story.builder()
        .text("Today You Can Find Us On\n" +
            "Wacker & Adams\n" +
            "Wabash & Jackson\n" +
            "58th & Ellis\n" +
            "Or Our Shop Inside \n" +
            "The Chicago French Market http://fb.me/VyHs8HmW")
        .time(tweetTime)
        .build();
    geolocate("Wacker and Adams, Chicago, IL", loc1);
    geolocate("Wabash and Jackson, Chicago, IL", loc2);
    geolocate("University of Chicago", loc3);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    beaverMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(2);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc2);
    assertThat(match.getAdditionalStops()
        .get(1)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getLocation()).isEqualTo(loc3);

  }

  @Test
  public void test3() {
    Story story = Story.builder()
        .text("Hot.Fresh.Made To Order\n" +
            "Open Now On\n" +
            "Wacker & Madison\n" +
            "Wacker & Adams\n" +
            "And Inside @ChiFrenchMarket\n" +
            "#DamGoodDonuts ")
        .time(tweetTime)
        .build();
    geolocate("Wacker and Adams, Chicago, IL", loc1);
    geolocate("Madison and Wacker, Chicago, IL", loc3);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    beaverMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(10, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc3);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(1);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getStop()
        .getNotes()
        .get(0)).isEqualTo(
        "Stop added from twitter story: 'Hot.Fresh.Made To Order Open Now On Wacker & Madison Wacker & Adams And Inside @ChiFrenchMarket #DamGoodDonuts' at 8/3/16 10:16 AM");

  }

  @Test
  public void test4() {
    Story story = Story.builder()
        .text("Catch Us Today On\n" +
            "Wabash & Jackson\n" +
            "Wacker & Adams\n" +
            "And Inside @ChiFrenchMarket \n" +
            "#ThinkSpring ")
        .time(tweetTime)
        .build();
    geolocate("Wabash and Jackson, Chicago, IL", loc1);
    geolocate("Wacker and Adams, Chicago, IL", loc2);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    beaverMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(1);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc2);

  }

  @Test
  public void test5() {
    Story story = Story.builder()
        .text("Open At Our Usual Sunday Spots\n" +
            "On Southport Till 2\n" +
            "And In West Loop\n" +
            "On Sangamon & Monroe Till 1")
        .time(tweetTime)
        .build();
    geolocate("Southport and Addison, Chicago, IL", loc1);
    geolocate("Sangamon and Monroe, Chicago, IL", loc2);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    beaverMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(8, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(1);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(8, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(13, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc2);

  }

  @Test
  public void test6() {
    tweetTime = new DateTime(2016, 1, 8, 7, 30, 0, 0, DateTimeZone.UTC);
    Story story = Story.builder()
        .text("Find Us This Morning\n" +
            "On Wacker & Adams\n" +
            "Clark & Adams\n" +
            "And Inside @ChiFrenchMarket\n" +
            "#TGIF")
        .time(tweetTime)
        .build();
    geolocate("Wacker and Adams, Chicago, IL", loc1);
    geolocate("Clark and Adams, Chicago, IL", loc2);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    beaverMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(1);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc2);

  }

  @Test
  public void test7() {
    tweetTime = new DateTime(2016, 1, 8, 7, 30, 0, 0, DateTimeZone.UTC);
    Story story = Story.builder()
        .text("The best donuts in Chicago! Coming out fried to order at:\n" +
            "-Wacker/Madison\n" +
            "-Wacker/Adams\n" +
            "-Clark/Adams\n" +
            "- &inside @ChiFrenchMarket\n" +
            "#noSnowDay")
        .time(tweetTime)
        .build();
    geolocate("Madison and Wacker, Chicago, IL", loc1);
    geolocate("Clark and Adams, Chicago, IL", loc2);
    geolocate("Wacker and Adams, Chicago, IL", loc3);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    beaverMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(10, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(2);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc3);
    assertThat(match.getAdditionalStops()
        .get(1)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getLocation()).isEqualTo(loc2);

  }

  @Test
  public void test9() {
    tweetTime = new DateTime(2016, 1, 8, 7, 30, 0, 0, DateTimeZone.UTC);
    Story story = Story.builder()
        .text("It's #LeapDay \n" +
            "Great Reason To Eat Donuts!\n" +
            "Open On\n" +
            "Wacker & Adams\n" +
            "Wacker & Madison\n" +
            "Clark & Adams\n" +
            "&  @ChiFrenchMarket ")
        .time(tweetTime)
        .build();
    geolocate("Madison and Wacker, Chicago, IL", loc1);
    geolocate("Clark and Adams, Chicago, IL", loc2);
    geolocate("Wacker and Adams, Chicago, IL", loc3);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    beaverMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(10, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(2);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc3);
    assertThat(match.getAdditionalStops()
        .get(1)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getLocation()).isEqualTo(loc2);


  }

  @Test
  public void test10() {
    tweetTime = new DateTime(2016, 1, 8, 7, 30, 0, 0, DateTimeZone.UTC);
    Story story = Story.builder()
        .text("Start Your Day\n" +
            "The Beavers Way\n" +
            "Open On\n" +
            "Wacker & Madison\n" +
            "Wacker & Adams\n" +
            "Clark & Adams\n" +
            "& Inside @ChiFrenchMarket\n" +
            "#TGIF ")
        .time(tweetTime)
        .build();
    geolocate("Madison and Wacker, Chicago, IL", loc1);
    geolocate("Clark and Adams, Chicago, IL", loc2);
    geolocate("Wacker and Adams, Chicago, IL", loc3);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    beaverMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(10, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(2);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc3);
    assertThat(match.getAdditionalStops()
        .get(1)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(1)
        .getLocation()).isEqualTo(loc2);

  }

  @Test
  public void test11() {
    tweetTime = new DateTime(2016, 1, 8, 7, 30, 0, 0, DateTimeZone.UTC);
    Story story = Story.builder()
        .text("Open Now Inside\n" +
            "@ChiFrenchMarket \n" +
            "Or On:\n" +
            "Wacker\n" +
            "Or\n" +
            "Wabash & Jackson")
        .time(tweetTime)
        .build();
    geolocate("Wacker and Adams, Chicago, IL", loc2);
    geolocate("Wabash and Jackson, Chicago, IL", loc1);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    beaverMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(1);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc2);

  }

  @Test
  public void test12() {
    tweetTime = new DateTime(2016, 1, 8, 7, 30, 0, 0, DateTimeZone.UTC);
    Story story = Story.builder()
        .text("Open Now On\n" +
            "Wacker Drive,\n" +
            "@UChicago \n" +
            "& Inside @ChiFrenchMarket \n" +
            "#DamGoodDonuts")
        .time(tweetTime)
        .build();
    geolocate("Wacker and Adams, Chicago, IL", loc2);
    geolocate("University of Chicago", loc1);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();

    beaverMatch.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getStop()
        .getLocation()).isEqualTo(loc1);
    assertThat(match.getAdditionalStops()
        .size()).isEqualTo(1);
    assertThat(match.getAdditionalStops()
        .get(0)
        .getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
    assertThat(match.getAdditionalStops()
        .get(0)
        .getLocation()).isEqualTo(loc2);

  }

  private void geolocate(String address, Location location) {
    when(geoLocator.locate(address, GeolocationGranularity.NARROW)).thenReturn(location);
  }
}