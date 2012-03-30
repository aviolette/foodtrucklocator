package foodtruck.schedule;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.DayOfWeek;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TweetSummary;
import foodtruck.util.Clock;
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
  private Clock clock;

  @Before
  public void before() {
    extractor = createMock(AddressExtractor.class);
    geolocator = createMock(GeoLocator.class);
    clock = createMock(Clock.class);
    expect(clock.dayOfWeek()).andStubReturn(DayOfWeek.sunday);
    topic = new TruckStopMatcher(extractor, geolocator, DateTimeZone.UTC, clock);
    truck = new Truck.Builder().id("foobar").build();
    tweetTime = new DateTime(2011, 11, 10, 11, 13, 7, 7, DateTimeZone.UTC);
  }

  @After
  public void after() {
    verifyAll();
  }

  @Test
  public void testMatch_shouldReturnNullWhenNoAddress() {
    final String tweetText = "foobar";
    expect(extractor.parse(tweetText, truck)).andReturn(ImmutableList.<String>of());
    replayAll();
    TweetSummary tweet = new TweetSummary.Builder().text(tweetText).time(tweetTime).build();
    TruckStopMatch match = topic.match(truck, tweet, null);
    assertNull(match);
  }

  @Test
  public void testMatch_shouldReturnNullWhenUnableToGeolocate() {
    TruckStopMatch match =
        tweet("Culture: Last call Erie and Kingsbury, outta here in 15 minutes, " +
            "then off to our next River North location, Hubbard & LaSalle")
            .geolocatorReturns(null)
            .match();
    assertNull(match);
  }


  @Test
  public void testMatch_shouldReturnHighConfidenceWhenAtLocationUntil() {
    TruckStopMatch match =
        tweet("Gold Coast, we have landed at Rush and Walton...here until 6 pm!")
            .withTime(tweetTime)
            .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(18, 0, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_shouldReturnHighConfidenceWhenAtLocationTil() {
    TruckStopMatch match = tweet("Gold Coast, we have landed at Rush and Walton...here til 6 pm!")
        .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(18, 0, 0, 0), match.getStop().getEndTime());
  }

  // til autocorrects to till so handle that too
  @Test
  public void testMatch_shouldReturnHighConfidenceWhenAtLocationTill() {
    TruckStopMatch match = tweet("Gold Coast, we have landed at Rush and Walton...here till 6 pm!")
        .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(18, 0, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_anotherUntil() {
    TruckStopMatch match = tweet("At 353 N Desplaines until 8pm with @bigstarchicago  !!")
        .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(20, 0, 0, 0), match.getStop().getEndTime());
  }


  @Test
  public void testMatch_includesCurrentDayOfTheWeek() {
    TruckStopMatch match = tweet("SweetSpotMac: Arrived at Michigan and Walton. " +
        "Come get your Sunday macaron going!")
        .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
  }

  @Test
  public void testMatch_shouldReturnMatch() {
    TruckStopMatch match =
        tweet("Oh yea oh yea beautiful night in the Chi. " +
            "Come get ur froyo fix we are on the corner of Michigan and Ohio!")
            .geolocatorReturns(Location.builder().lat(-1).lng(-2).name("Michigan and Ohio").build())
            .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals("Michigan and Ohio", match.getStop().getLocation().getName());
    verifyAll();
  }

  @Test
  public void testMatch_shouldDetectTimeRange() {
    tweetTime = new DateTime(2011, 11, 12, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match =
        tweet("The tamalespaceship will be landing at our weekly spot <<Dearborn & Monroe>> " +
            "11a.m.-1:30p.m. last chance to get your tamale fix before the weekend!!")
            .withTime(tweetTime)
            .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(match.getStop().getStartTime(), tweetTime.withTime(11, 0, 0, 0));
    assertEquals(match.getStop().getEndTime(), tweetTime.withTime(13, 30, 0, 0));
  }

  @Test
  public void testMatch_shouldDetectTimeRangeNoon() {
    tweetTime = new DateTime(2011, 11, 12, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("The tamalespaceship will be landing at our weekly spot " +
        "<<Dearborn & Monroe>> 11a.m.-noon. last chance to get your tamale fix before the weekend!!")
        .withTime(tweetTime)
        .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(match.getStop().getStartTime(), tweetTime.withTime(11, 0, 0, 0));
    assertEquals(match.getStop().getEndTime(), tweetTime.withTime(12, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldDetectFutureLocation() {
    tweetTime = new DateTime(2011, 11, 12, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Rush & UIC Medical Center DonRafa is gonna be in ur area today!" +
        " Don't want to come out? call 312-498-9286 we... fb.me/1gKduQrvS")
        .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(match.getStop().getStartTime(), tweetTime.withTime(11, 30, 0, 0));
  }

  @Test
  public void testMatch_shouldNotDetectFutureLocationIfBreakfastTruck() {
    tweetTime = new DateTime(2011, 11, 12, 7, 0, 0, 0, DateTimeZone.UTC);
    truck = new Truck.Builder().id("foobar").name("FOO").twitterHandle("bar")
        .categories(ImmutableSet.of("Breakfast")).build();
    TruckStopMatch match =
        tweet("BeaversDonuts: Good Morning! The window is open at Erie and Franklin in front " +
            "of @FlairTower222, come on over we are here till 9ish.")
            .withTruck(truck)
            .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(match.getStop().getStartTime(), tweetTime);
  }

  // for now, we can't handle tweets like this.
  @Test
  public void testMatch_shouldntMatchDayOfWeek() {
    TruckStopMatch match =
        tweet("5411empanadas: MON: Oak and Michigan / TUE: Univ of Chicago (Hyde Park) " +
            "/ WED: Dearborn & Monroe / THU: Columbus & Randolph / FRI: Wacker & Van Buren")
            .match();
    assertNull(match);
  }

  @Test
  public void testMatch_shouldMatchTodaysSchedule() {
    TruckStopMatch match = tweet("SweetRideChi: TUES STOPS:  1130a - Taylor & Wood\n" +
        "1245p - UIC Campus Vernon Park Circle by BSB bldg\n" +
        "245p - Wacker & Lasalle\n" +
        "430p... http://t.co/EDVtU2XM").match();
    assertNull(match);
  }

  // for now, we can't handle tweets like this.
  @Test
  public void testMatch_shouldntMatchDayOfWeek2() {
    expect(clock.dayOfWeek()).andReturn(DayOfWeek.sunday);
    tweetTime = new DateTime(2011, 11, 12, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match =
        tweet("We hope you having a great weekend, see you on Monday <<Wells & Monroe>>")
            .match();
    assertNull(match);
  }

  @Test
  public void testMatch_shouldMatchDayOfWeekIfCurrentDay() {
    tweetTime = new DateTime(2011, 11, 6, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match =
        tweet("GiGisBakeShop: Hello SUNDAY!  The PURPLE Bus is headed out...Look for us at " +
            "13th / S Michigan 11:15 am, Lincoln Square 1:30 pm")
            .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(match.getStop().getStartTime(), tweetTime.withTime(11, 30, 0, 0));
  }

  @Test
  public void testMatch_shouldNotMatchDayOfWeekIfTomorrow() {
    tweetTime = new DateTime(2011, 11, 7, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("@5411empanadas ahhh no uofc tues?? I shall starve").match();
    assertNull(match);
  }

  @Test
  public void testMatch_shouldNotMatchWhenHashNotPresent() {
    truck = new Truck.Builder(truck).matchOnlyIf("#bunsontherun").build();
    TruckStopMatch match = tweet(
        "Oooops on the handshake between Chris and Taylor - next time try a fistbump #AMA2011")
        .noParse()
        .withTruck(truck)
        .match();
    assertNull(match);
    verifyAll();
  }

  @Test
  public void testMatch_shouldMatchWhenHasMatchOnlyIfExpression() {
    truck = new Truck.Builder(truck).matchOnlyIf("#bunsontherun").build();
    TruckStopMatch match = tweet(
        "Oooops on the handshake between Chris and Taylor - next time try a fistbump #BunsOnTheRun")
        .withTruck(truck)
        .match();
    assertNotNull(match);
    assertEquals(Confidence.HIGH, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
  }

  @Test
  public void testMatch_shouldNotMatchWhenRetweet() {
    assertNull(tweet("Mmmm... RT @theslideride we are on Clinton & Lake").noParse().match());
  }

  @Test
  public void testMatch_shouldNotMatchWhenRetweetWithNoPreceedingText() {
    assertNull(tweet("RT @theslideride we are on Clinton & Lake").noParse().match());
  }

  public Tweeter tweet(String tweet) {
    return new Tweeter(tweet);
  }

  private class Tweeter {
    private String tweet;
    private Truck truck;
    private DateTime time;
    private String address = "Foo and Bar";
    private Location geolocatorResult;
    private boolean expectParse = true;

    public Tweeter(String tweet) {
      Tweeter.this.tweet = tweet;
      Tweeter.this.truck = TruckStopMatcherTest.this.truck;
      Tweeter.this.time = TruckStopMatcherTest.this.tweetTime;
      this.geolocatorResult = Location.builder().lat(-1).lng(-2).name(address).build();
    }

    public Tweeter withTruck(Truck truck) {
      Tweeter.this.truck = truck;
      return this;
    }

    public Tweeter withTime(DateTime time) {
      this.time = time;
      return this;
    }

    public Tweeter geolocatorReturns(@Nullable Location location) {
      this.geolocatorResult = location;
      return this;
    }

    public TruckStopMatch match() {
      if (expectParse) {
        expect(extractor.parse(tweet, Tweeter.this.truck)).andReturn(ImmutableList.of(address));
        expect(geolocator.locate(address, GeolocationGranularity.NARROW))
            .andReturn(geolocatorResult);
      }
      replayAll();
      TweetSummary tweet = new TweetSummary.Builder().text(Tweeter.this.tweet)
          .time(Tweeter.this.time).build();
      return topic.match(Tweeter.this.truck, tweet, null);
    }

    public Tweeter noParse() {
      expectParse = false;
      return this;
    }
  }

}
