package foodtruck.schedule;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
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
    topic = new TruckStopMatcher(extractor, geolocator, DateTimeZone.UTC);
    truck = new Truck.Builder().id("foobar").build();
    tweetTime = new DateTime(2011, 11, 10, 9, 8, 7, 7, DateTimeZone.UTC);
  }

  @Test
  public void testMatch_shouldReturnNullWhenNoAddress() {
    final String tweetText = "foobar";
    expect(extractor.parseFirst(tweetText)).andReturn(null);
    replayAll();
    TweetSummary tweet = new TweetSummary.Builder().text(tweetText).time(tweetTime).build();
    TruckStopMatch match = topic.match(truck, tweet);
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
    TruckStopMatch match = topic.match(truck, tweet);
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
    TruckStopMatch match = topic.match(truck, tweet);
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(18, 0, 0, 0), match.getStop().getEndTime());
    verifyAll();
  }
}
