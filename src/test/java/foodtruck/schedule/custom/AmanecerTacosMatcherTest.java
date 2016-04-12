package foodtruck.schedule.custom;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.schedule.TruckStopMatch;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author aviolette
 * @since 4/10/16
 */
public class AmanecerTacosMatcherTest extends EasyMockSupport {
  private GeoLocator geoLocator;
  private AmanecerTacosMatcher matcher;
  private DateTime tweetTime;
  private Location loc1, loc2, loc3;
  private Truck truck;

  @Before
  public void setup() {
    truck = Truck.builder().id("amanecertacos").build();
    loc1 = Location.builder().name("Location 1").lat(1).lng(1).build();
    loc3 = Location.builder().name("Location 2").lat(2).lng(2).build();
    loc2 = Location.builder().name("Location 3").lat(1).lng(1).build();
    geoLocator = createMock(GeoLocator.class);
    tweetTime = new DateTime(2016, 1, 11, 7, 30, 0, 0, DateTimeZone.UTC);
    matcher = new AmanecerTacosMatcher(geoLocator);
  }

  @Test
  public void testHandle1() {
    Story story = Story.builder().text("It's #FoodTruckFriday and this morning we are at:\n" +
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
    replayAll();
    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertNotNull(match.getStop());
    assertEquals(tweetTime.withTime(8, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(8, 45, 0, 0), match.getStop().getEndTime());
    assertEquals(loc1, match.getStop().getLocation());
    assertEquals(2, match.getAdditionalStops().size());
    assertEquals(tweetTime.withTime(9, 0, 0, 0), match.getAdditionalStops().get(0).getStartTime());
    assertEquals(tweetTime.withTime(9, 45, 0, 0), match.getAdditionalStops().get(0).getEndTime());
    assertEquals(loc2,  match.getAdditionalStops().get(0).getLocation());
    assertEquals(tweetTime.withTime(10, 0, 0, 0), match.getAdditionalStops().get(1).getStartTime());
    assertEquals(tweetTime.withTime(10, 45, 0, 0), match.getAdditionalStops().get(1).getEndTime());
    assertEquals(loc3, match.getAdditionalStops().get(1).getLocation());
    verifyAll();
  }


  @Test
  public void testHandle2() {
    Story story = Story.builder().text("Today's #foodtruck schedule:\n" +
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
    replayAll();
    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertNotNull(match.getStop());
    assertEquals(tweetTime.withTime(8, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(8, 45, 0, 0), match.getStop().getEndTime());
    assertEquals(loc1, match.getStop().getLocation());
    assertEquals(2, match.getAdditionalStops().size());
    assertEquals(tweetTime.withTime(9, 0, 0, 0), match.getAdditionalStops().get(0).getStartTime());
    assertEquals(tweetTime.withTime(9, 45, 0, 0), match.getAdditionalStops().get(0).getEndTime());
    assertEquals(loc2,  match.getAdditionalStops().get(0).getLocation());
    assertEquals(tweetTime.withTime(10, 0, 0, 0), match.getAdditionalStops().get(1).getStartTime());
    assertEquals(tweetTime.withTime(10, 45, 0, 0), match.getAdditionalStops().get(1).getEndTime());
    assertEquals(loc3, match.getAdditionalStops().get(1).getLocation());
    verifyAll();
  }

  @Test @Ignore
  public void testHandle3() {
    Story story = Story.builder().text("We're on our regular schedule:  8AM Davis, 9AM Foster, 10AM Garrett. Usher in the weekend taco style!")
        .time(tweetTime)
        .build();
    geolocate("Davis Station", loc1);
    geolocate("Foster and Sheridan, Evanston, IL", loc1);
    geolocate("Garrett and Sheridan, Evanston, IL", loc3);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();
    replayAll();
    matcher.handle(builder, story, truck);
    TruckStopMatch match = builder.build();
    assertNotNull(match.getStop());
    assertEquals(tweetTime.withTime(8, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(8, 45, 0, 0), match.getStop().getEndTime());
    assertEquals(loc1, match.getStop().getLocation());
    assertEquals(2, match.getAdditionalStops().size());
    assertEquals(tweetTime.withTime(9, 0, 0, 0), match.getAdditionalStops().get(0).getStartTime());
    assertEquals(tweetTime.withTime(9, 45, 0, 0), match.getAdditionalStops().get(0).getEndTime());
    assertEquals(loc2,  match.getAdditionalStops().get(0).getLocation());
    assertEquals(tweetTime.withTime(10, 0, 0, 0), match.getAdditionalStops().get(1).getStartTime());
    assertEquals(tweetTime.withTime(10, 45, 0, 0), match.getAdditionalStops().get(1).getEndTime());
    assertEquals(loc3, match.getAdditionalStops().get(1).getLocation());
    verifyAll();
  }

  private void geolocate(String address, Location location) {
    expect(geoLocator.locate(address, GeolocationGranularity.NARROW))
        .andReturn(location);
  }
}