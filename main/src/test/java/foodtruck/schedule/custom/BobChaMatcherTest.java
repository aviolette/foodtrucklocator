package foodtruck.schedule.custom;

import com.google.common.collect.ImmutableList;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.schedule.Spot;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.custom.chicago.BobChaMatcher;
import foodtruck.time.Clock;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author aviolette
 * @since 4/9/16
 */
public class BobChaMatcherTest extends EasyMockSupport {


  private GeoLocator geoLocator;
  private BobChaMatcher matcher;
  private DateTime tweetTime;
  private Location loc1, loc2, loc3;
  private Truck truck;

  @Before
  public void setup() {
    truck = Truck.builder().id("bobchafoodtruck").build();
    loc1 = Location.builder().name("Location 1").lat(1).lng(1).build();
    loc3 = Location.builder().name("Location 2").lat(2).lng(2).build();
    loc2 = Location.builder().name("Location 3").lat(1).lng(1).build();
    geoLocator = createMock(GeoLocator.class);
    tweetTime = new DateTime(2016, 1, 11, 7, 30, 0, 0, DateTimeZone.UTC);
    ImmutableList<Spot> commonSpots = ImmutableList.of(
        new Spot("600w", "600 West Chicago Avenue, Chicago, IL"),
        new Spot("wabash/vanburen", "Wabash and Van Buren, Chicago, IL"),
        new Spot("wacker/adams", "Wacker and Adams, Chicago, IL"),
        new Spot("clark/adams", "Clark and Adams, Chicago, IL"),
        new Spot("harrison/michigan", "Michigan and Harrison, Chicago, IL"),
        new Spot("lasalle/adams", "Lasalle and Adams, Chicago, IL"),
        new Spot("clark/monroe", "Clark and Monroe, Chicago, IL"),
        new Spot("wabash/jackson", "Wabash and Jackson, Chicago, IL"),
        new Spot("uchicago", "University of Chicago"),
        new Spot("uofc", "University of Chicago"),
        new Spot("58th/ellis", "University of Chicago"));
    Clock clock = createMock(Clock.class);
    expect(clock.now()).andStubReturn(new DateTime(1470237365629L));
    matcher = new BobChaMatcher(geoLocator, commonSpots, DateTimeFormat.forStyle("SS"), clock);
  }

  @Test
  public void testHandleWeekend() {
    tweetTime = new DateTime(2016, 1, 8, 7, 30, 0, 0, DateTimeZone.UTC);
    Story story = Story.builder()
        .text("We will be open for business at U of C, and Clark & Monroe till 1:30pm!!")
        .time(tweetTime.withTime(11, 0, 0, 0))
        .userId("")
        .build();
    TruckStopMatch.Builder builder = TruckStopMatch.builder();
    replayAll();
    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertNull(match.getStop());
    verifyAll();
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
    replayAll();
    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(14, 0, 0, 0), match.getStop().getEndTime());
    assertEquals(loc1, match.getStop().getLocation());
    assertEquals(1, match.getAdditionalStops().size());
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getAdditionalStops().get(0).getStartTime());
    assertEquals(tweetTime.withTime(14, 0, 0, 0), match.getAdditionalStops().get(0).getEndTime());
    assertEquals(loc2, match.getAdditionalStops().get(0).getLocation());
    assertEquals(truck, match.getAdditionalStops().get(0).getTruck());
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
    replayAll();
    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(14, 0, 0, 0), match.getStop().getEndTime());
    assertEquals(loc1, match.getStop().getLocation());
    assertEquals(1, match.getAdditionalStops().size());
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getAdditionalStops().get(0).getStartTime());
    assertEquals(tweetTime.withTime(14, 0, 0, 0), match.getAdditionalStops().get(0).getEndTime());
    assertEquals(loc2, match.getAdditionalStops().get(0).getLocation());
    assertEquals(truck, match.getAdditionalStops().get(0).getTruck());
  }

  private void geolocate(String address, Location location) {
    expect(geoLocator.locate(address, GeolocationGranularity.NARROW))
        .andReturn(location);
  }
}