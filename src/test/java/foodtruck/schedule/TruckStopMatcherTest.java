package foodtruck.schedule;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TweetSummary;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author aviolette@gmail.com
 * @since 9/23/11
 */
public class TruckStopMatcherTest extends EasyMockSupport {

  private AddressExtractor extractor;
  private GeoLocator geolocator;
  private TruckStopMatcher topic;
  private Truck truck;
  private DateTime tweetTime;

  @Before
  public void before() {
    extractor = createMock(AddressExtractor.class);
    geolocator = createMock(GeoLocator.class);
    topic = new TruckStopMatcher(extractor, geolocator, DateTimeZone.UTC, null);
    truck = new Truck.Builder().id("foobar").build();
    tweetTime = new DateTime(2011, 11, 10, 11, 13, 7, 7, DateTimeZone.UTC);
  }

  @Test
  public void testMatch_shouldReturnNullWhenNoAddress() {
    final String tweetText = "foobar";
    expect(extractor.parseFirst(tweetText)).andReturn(null);
    replayAll();
    TweetSummary tweet = new TweetSummary.Builder().text(tweetText).time(tweetTime).build();
    TruckStopMatch match = topic.match(truck, tweet, null);
    assertNull(match);
    verifyAll();
  }

  @Test
  public void testMatch_shouldReturnNullWhenUnableToGeolocate() {
    final String tweetText = "Culture: Last call Erie and Kingsbury, outta here in 15 minutes, " +
        "then off to our next River North location, Hubbard & LaSalle";
    final String address = "Erie and Kingsbury";
    expect(extractor.parseFirst(tweetText)).andReturn(address);
    expect(geolocator.locate(address)).andReturn(null);
    replayAll();
    TweetSummary tweet = new TweetSummary.Builder().text(tweetText).time(tweetTime).build();
    TruckStopMatch match = topic.match(truck, tweet, null);
    assertNull(match);
    verifyAll();
  }

  @Test
  public void testMatch_shouldReturnHighConfidenceWhenAtLocationUntil() {
    final String tweetText = "Gold Coast, we have landed at Rush and Walton...here until 6 pm!";
    final String address = "Rush and Walton";
    Location location = new Location(-1, -2, address);
    expect(extractor.parseFirst(tweetText)).andReturn(address);
    expect(geolocator.locate(address)).andReturn(location);
    replayAll();
    TweetSummary tweet = new TweetSummary.Builder().text(tweetText).time(tweetTime).build();
    TruckStopMatch match = topic.match(truck, tweet, null);
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(18, 0, 0, 0), match.getStop().getEndTime());
    verifyAll();
  }


  @Test @Ignore
  public void testMatch_includesCurrentDayOfTheWeek() {
    final String tweetText =
        "SweetSpotMac: Arrived at Michigan and Walton. Come get your Sunday macaron going!";
    final String address = "Michigan and Walton";
    Location location = new Location(-1, -2, address);
    expect(extractor.parseFirst(tweetText)).andReturn(address);
    expect(geolocator.locate(address)).andReturn(location);
    replayAll();
    TweetSummary tweet = new TweetSummary.Builder().text(tweetText).time(tweetTime).build();
    TruckStopMatch match = topic.match(truck, tweet, null);
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(18, 0, 0, 0), match.getStop().getEndTime());
    verifyAll();
  }

  @Test
  public void testMatch_shouldReturnMatch() {
    final String tweetText =
        "Oh yea oh yea beautiful night in the Chi. Come get ur froyo fix we are on the corner of Michigan and Ohio!";
    final String address = "Michigan and Ohio";
    Location location = new Location(-1, -2, address);
    expect(extractor.parseFirst(tweetText)).andReturn(address);
    expect(geolocator.locate(address)).andReturn(location);
    replayAll();
    TweetSummary tweet = new TweetSummary.Builder().text(tweetText).time(tweetTime).build();
    TruckStopMatch match = topic.match(truck, tweet, null);
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(address, match.getStop().getLocation().getName());
    verifyAll();
  }

  @Test
  public void testMatch_shouldDetectFutureLocation() {
    final String tweetText =
        "Rush & UIC Medical Center DonRafa is gonna be in ur area today! Don't want to come out? call 312-498-9286 we... fb.me/1gKduQrvS";
    final String address = "UIC";
    Location location = new Location(-1, -2, address);
    expect(extractor.parseFirst(tweetText)).andReturn(address);
    expect(geolocator.locate(address)).andReturn(location);
    tweetTime = new DateTime(2011, 11, 12, 9, 0, 0, 0, DateTimeZone.UTC);
    replayAll();
    TweetSummary tweet = new TweetSummary.Builder().text(tweetText).time(tweetTime).build();
    TruckStopMatch match = topic.match(truck, tweet, null);
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(address, match.getStop().getLocation().getName());
    assertEquals(match.getStop().getStartTime(), tweetTime.withTime(11, 30, 0, 0));
    verifyAll();
  }

  // for now, we can't handle tweets like this.
  @Test
  public void testMatch_shouldntMatchDayOfWeek() {
    final String tweetText =
        "5411empanadas: MON: Oak and Michigan / TUE: Univ of Chicago (Hyde Park) / WED: Dearborn & Monroe / THU: Columbus & Randolph / FRI: Wacker & Van Buren";
    final String address = "Oak and Michigan";
    Location location = new Location(-1, -2, address);
    expect(extractor.parseFirst(tweetText)).andReturn(address);
    expect(geolocator.locate(address)).andReturn(location);
    tweetTime = new DateTime(2011, 11, 12, 9, 0, 0, 0, DateTimeZone.UTC);
    replayAll();
    TweetSummary tweet = new TweetSummary.Builder().text(tweetText).time(tweetTime).build();
    TruckStopMatch match = topic.match(truck, tweet, null);
    assertNull(match);
    verifyAll();
  }

  @Test
  public void testMatch_shouldMatchTodaysSchedule() {
    final String tweetText = "SweetRideChi: TUES STOPS:  1130a - Taylor & Wood\n" +
        "1245p - UIC Campus Vernon Park Circle by BSB bldg\n" +
        "245p - Wacker & Lasalle\n" +
        "430p... http://t.co/EDVtU2XM";
    final String address = "Taylor & Wood";
    Location location = new Location(-1, -2, address);
    expect(extractor.parseFirst(tweetText)).andReturn(address);
    expect(geolocator.locate(address)).andReturn(location);
    replayAll();
    TweetSummary tweet = new TweetSummary.Builder().text(tweetText).time(tweetTime).build();
    TruckStopMatch match = topic.match(truck, tweet, null);
    assertNull(match);
    verifyAll();
  }

  // for now, we can't handle tweets like this.
  @Test
  public void testMatch_shouldntMatchDayOfWeek2() {
    final String tweetText =
        "We hope you having a great weekend, see you on Monday <<Wells & Monroe>> pic.twitter.com/1ewdrgKF";
    final String address = "Wells and Monroe";
    Location location = new Location(-1, -2, address);
    expect(extractor.parseFirst(tweetText)).andReturn(address);
    expect(geolocator.locate(address)).andReturn(location);
    tweetTime = new DateTime(2011, 11, 12, 9, 0, 0, 0, DateTimeZone.UTC);
    replayAll();
    TweetSummary tweet = new TweetSummary.Builder().text(tweetText).time(tweetTime).build();
    TruckStopMatch match = topic.match(truck, tweet, null);
    assertNull(match);
    verifyAll();
  }

  // TODO: Handle this: tamalespace101: The tamalespaceship will be landing at <<324 S Racine>> @ethylsdive loaded with delicious tamales for dinner 6-9pm come and get them!!
}
