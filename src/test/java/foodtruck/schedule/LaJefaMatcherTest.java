package foodtruck.schedule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author aviolette
 * @since 3/30/16
 */
public class LaJefaMatcherTest extends EasyMockSupport {

  private GeoLocator geoLocator;
  private LaJefaMatcher laJefaMatch;
  private DateTime tweetTime;
  private Location loc1, loc2, loc3;
  private TruckDAO truckDAO;
  private Truck patrona, lajefa;

  @Before
  public void setup() {
    patrona = Truck.builder().id("patronachicago").categories(ImmutableSet.of("Mexican")).build();
    lajefa = Truck.builder().id("lajefa").categories(ImmutableSet.of("Mexican")).build();

    truckDAO = createMock(TruckDAO.class);
    loc1 = Location.builder().name("Location 1").lat(1).lng(1).build();
    loc3 = Location.builder().name("Location 2").lat(2).lng(2).build();
    loc2 = Location.builder().name("Location 3").lat(1).lng(1).build();
    geoLocator = createMock(GeoLocator.class);
    tweetTime = new DateTime(2016, 1, 8, 7, 30, 0, 0, DateTimeZone.UTC);
    ImmutableList<Spot> commonSpots = ImmutableList.of(new Spot("600w", "600 West Chicago Avenue, Chicago, IL"),
        new Spot("wabash/vanburen", "Wabash and Van Buren, Chicago, IL"),
        new Spot("wacker/adams", "Wacker and Adams, Chicago, IL"),
        new Spot("clark/adams", "Clark and Adams, Chicago, IL"),
        new Spot("harrison/michigan", "Michigan and Harrison, Chicago, IL"),
        new Spot("lasalle/adams", "Lasalle and Adams, Chicago, IL"),
        new Spot("clark/monroe", "Clark and Monroe, Chicago, IL"),
        new Spot("wabash/jackson", "Wabash and Jackson, Chicago, IL"), new Spot("uchicago", "University of Chicago"),
        new Spot("58th/ellis", "University of Chicago"));
    laJefaMatch = new LaJefaMatcher(truckDAO, commonSpots, geoLocator);
    expect(truckDAO.findById("lajefa")).andStubReturn(lajefa);

  }

  @Test
  public void testNoMatch() throws Exception {
    Story story = Story.builder().text("Haven't had lunch yet? We're on Wacker and Adams until 3pm! La Jefa is on lasalle and Adams until 3pm too!")
        .time(tweetTime)
        .build();
    geolocate("Lasalle and Adams, Chicago, IL", loc2);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();
    replayAll();
    laJefaMatch.handle(builder, story, patrona);
    TruckStopMatch match = builder.build();
    assertNull(match.getStop());
  }

  @Test
  public void testHandle1() throws Exception {
    Story story = Story.builder().text("Haven't had lunch yet? We're on Wacker and Adams until 3pm! La Jefa is on lasalle and Adams until 3pm too!")
        .time(tweetTime)
        .build();
    geolocate("Lasalle and Adams, Chicago, IL", loc2);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();
    builder.appendStop(TruckStop.builder()
        .truck(patrona)
        .endTime(tweetTime.withTime(15, 0, 0, 0))
        .location(loc3)
        .startTime(tweetTime)
        .build());
    replayAll();
    laJefaMatch.handle(builder, story, patrona);
    TruckStopMatch match = builder.build();
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(15, 0, 0, 0), match.getStop().getEndTime());
    assertEquals(loc3, match.getStop().getLocation());
    assertEquals(1, match.getAdditionalStops().size());
    assertEquals(tweetTime, match.getAdditionalStops().get(0).getStartTime());
    assertEquals(tweetTime.withTime(15, 0, 0, 0), match.getAdditionalStops().get(0).getEndTime());
    assertEquals(loc2, match.getAdditionalStops().get(0).getLocation());
    assertEquals(lajefa, match.getAdditionalStops().get(0).getTruck());
  }

  @Test
  public void testHandle2() throws Exception {
    Story story = Story.builder().text("Wacker and Adams La Jefa at Clark and Monroe! Don't miss out on this beautiful weather!  Fresh meat in the grill! ")
        .time(tweetTime)
        .build();
    geolocate("Clark and Monroe, Chicago, IL", loc2);
    TruckStopMatch.Builder builder = TruckStopMatch.builder();
    builder.appendStop(
        TruckStop.builder().truck(patrona).endTime(tweetTime.withTime(15, 0, 0, 0)).location(loc3).startTime(tweetTime).build());
    replayAll();
    laJefaMatch.handle(builder, story, patrona);
    TruckStopMatch match = builder.build();
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(15, 0, 0, 0), match.getStop().getEndTime());
    assertEquals(loc3, match.getStop().getLocation());
    assertEquals(1, match.getAdditionalStops().size());
    assertEquals(tweetTime, match.getAdditionalStops().get(0).getStartTime());
    assertEquals(tweetTime.withTime(15, 0, 0, 0), match.getAdditionalStops().get(0).getEndTime());
    assertEquals(loc2,  match.getAdditionalStops().get(0).getLocation());
    assertEquals(lajefa, match.getAdditionalStops().get(0).getTruck());
  }

  private void geolocate(String address, Location location) {
    expect(geoLocator.locate(address, GeolocationGranularity.NARROW))
        .andReturn(location);
  }

}