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
import foodtruck.model.TruckStop;
import foodtruck.schedule.Spot;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.custom.chicago.CajunConMatcher;
import foodtruck.util.Clock;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @author aviolette
 * @since 5/11/16
 */
public class CajunConMatcherTest extends EasyMockSupport {

  private Truck truck;
  private Location loc1, loc2, loc3;
  private GeoLocator geoLocator;
  private DateTime tweetTime;
  private CajunConMatcher matcher;

  @Before
  public void setup() {
    truck = Truck.builder().id("thecajuncon").build();
    loc1 = Location.builder().name("Location 1").lat(1).lng(1).build();
    loc3 = Location.builder().name("Location 2").lat(2).lng(2).build();
    loc2 = Location.builder().name("Location 3").lat(1).lng(1).build();
    geoLocator = createMock(GeoLocator.class);
    tweetTime = new DateTime(2016, 1, 11, 7, 30, 0, 0, DateTimeZone.UTC);
    Clock clock = createMock(Clock.class);
    expect(clock.now()).andStubReturn(new DateTime(1470237365629L));
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
    builder.stop(TruckStop.builder().location(loc1)
        .truck(truck)
        .endTime(tweetTime.withTime(8, 30, 0, 0))
        .startTime(tweetTime.withTime(6, 0, 0, 0))
        .build());
    geolocate("1100 South Hamilton, Chicago, IL", loc2);
    replayAll();
    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertEquals(tweetTime.withTime(6, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(8, 30, 0, 0), match.getStop().getEndTime());
    assertEquals(loc1, match.getStop().getLocation());
    assertEquals(1, match.getAdditionalStops().size());
    assertEquals(tweetTime.withTime(10, 30, 0, 0), match.getAdditionalStops().get(0).getStartTime());
    assertEquals(tweetTime.withTime(13, 30, 0, 0), match.getAdditionalStops().get(0).getEndTime());
    assertEquals(loc2, match.getAdditionalStops().get(0).getLocation());

    verifyAll();
  }


  private void geolocate(String address, Location location) {
    expect(geoLocator.locate(address, GeolocationGranularity.NARROW))
        .andReturn(location);
  }
}