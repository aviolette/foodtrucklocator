package foodtruck.schedule;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.email.EmailNotifier;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Configuration;
import foodtruck.model.DayOfWeek;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TweetSummary;
import foodtruck.util.Clock;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

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
  private Location mapCenter;
  private ConfigurationDAO configDAO;
  private EmailNotifier notifier;

  @Before
  public void before() {
    extractor = createMock(AddressExtractor.class);
    geolocator = createMock(GeoLocator.class);
    clock = createMock(Clock.class);
    notifier = createMock(EmailNotifier.class);
    mapCenter = Location.builder().lat(41.8807438).lng(-87.6293867).build();
    expect(clock.dayOfWeek()).andStubReturn(DayOfWeek.sunday);
    Configuration config = Configuration.builder().center(mapCenter).build();
    configDAO = createMock(ConfigurationDAO.class);
    expect(configDAO.find()).andStubReturn(config);
    topic = new TruckStopMatcher(extractor, geolocator, DateTimeZone.UTC, clock, configDAO, notifier);
    truck = Truck.builder().id("foobar").build();
    expect(clock.zone()).andStubReturn(DateTimeZone.UTC);
    expect(clock.currentDay()).andStubReturn(new LocalDate(2011, 11, 10));
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
  public void testMatch_shouldNotifyByEmailWhenNewLocation() {
    String tweet = "Last call Erie and Kingsbury, outta here in 15 minutes," +
        " then off to our next River North location, Hubbard & LaSalle";
    Location location =Location.builder().lat(41.889973).lng(-87.634024).name("foobar")
        .wasJustResolved(true).build();
    notifier.systemNotifyLocationAdded(location,
        new TweetSummary.Builder().userId("foobar").text(tweet).time(tweetTime).build(), truck);
    TruckStopMatch match =
        tweet(tweet)
            .geolocatorReturns(location)
            .match();
    assertNotNull(match);
  }

  @Test
  public void testMatch_shouldReturnHighConfidenceWhenAtLocationUntil() {
    TruckStopMatch match =
        tweet("Gold Coast, we have landed at Rush and Walton...here until 6 pm!")
            .withTime(tweetTime)
            .match();
    assertNotNull(match);
    assertEquals(Confidence.MEDIUM, match.getConfidence());
    assertThat(match.getStop().getNotes(), hasItems("Tweet received for location: 'Gold Coast, we have landed at Rush and Walton...here until 6 pm!'"));
    assertThat(match.getStop().getNotes(), hasItems("Presence of end time in tweet increased confidence."));
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(18, 0, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_shouldReturnHighConfidenceWhenLeavingAt() {
    tweetTime = tweetTime.withHourOfDay(16).withMinuteOfHour(16);
    TruckStopMatch match =
        tweet("Our final stop tonight is Grand & McClurg. We'll be leaving at 5:30pm, so come soon!")
            .withTime(tweetTime)
            .match();
    assertNotNull(match);
    assertEquals(Confidence.MEDIUM, match.getConfidence());
    assertThat(match.getStop().getNotes(), hasItems("Tweet received for location: 'Our final stop tonight is Grand & McClurg. We'll be leaving at 5:30pm, so come soon!'"));
    assertThat(match.getStop().getNotes(), hasItems("Presence of end time in tweet increased confidence."));
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(17, 30, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testWithReturn() {
    TruckStopMatch match = tweet("We are now open for business at 600 west Chicago! We will be serving until 8:30\n" +
        "\n" +
        "@600WestBuilding @Groupon @BellyChicago @BarryCallebaut")
        .withTime(tweetTime.withTime(7, 0, 0, 0))
        .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Breakfast")).build())
        .match();
    assertNotNull(match);
    assertEquals(tweetTime.withTime(8, 30, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testWith12() {
    TruckStopMatch match = tweet("Lake and LaSalle 12-2 Northwestern Campus 3-7")
        .withTime(tweetTime.withTime(7, 0, 0, 0))
        .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Cupcakes")).build())
        .match();
    assertNotNull(match);
    assertEquals(tweetTime.withTime(12, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(14, 0, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_shouldReturnHighConfidenceWhenAtLocationTil() {
    TruckStopMatch match = tweet("Gold Coast, we have landed at Rush and Walton...here til 6 pm!")
        .match();
    assertNotNull(match);
    assertEquals(Confidence.MEDIUM, match.getConfidence());
    assertThat(match.getStop().getNotes(), hasItems("Tweet received for location: 'Gold Coast, we have landed at Rush and Walton...here til 6 pm!'"));
    assertThat(match.getStop().getNotes(), hasItems("Presence of end time in tweet increased confidence."));
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(18, 0, 0, 0), match.getStop().getEndTime());
  }

  // til autocorrects to till so handle that too
  @Test
  public void testMatch_shouldReturnHighConfidenceWhenAtLocationTill() {
    TruckStopMatch match = tweet("Gold Coast, we have landed at Rush and Walton...here till 6 pm!")
        .match();
    assertNotNull(match);
    assertEquals(Confidence.MEDIUM, match.getConfidence());
    assertThat(match.getStop().getNotes(), hasItems("Tweet received for location: 'Gold Coast, we have landed at Rush and Walton...here till 6 pm!'"));
    assertThat(match.getStop().getNotes(), hasItems("Presence of end time in tweet increased confidence."));
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(18, 0, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_anotherUntil() {
    TruckStopMatch match = tweet("At 353 N Desplaines until 8pm with @bigstarchicago  !!")
        .match();
    assertNotNull(match);
    assertEquals(Confidence.MEDIUM, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(20, 0, 0, 0), match.getStop().getEndTime());
  }


  @Test
  public void testMatch_includesCurrentDayOfTheWeek() {
    TruckStopMatch match = tweet("SweetSpotMac: Arrived at Michigan and Walton. " +
        "Come get your Sunday macaron going!")
        .match();
    assertNotNull(match);
    assertEquals(Confidence.LOW, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
  }

  @Test
  public void testMatch_shouldReturnMatch() {
    truck = Truck.builder(truck).categories(ImmutableSet.of("Dessert")).build();
    TruckStopMatch match =
        tweet("Oh yea oh yea beautiful night in the Chi. " +
            "Come get ur froyo fix we are on the corner of Michigan and Ohio!")
            .geolocatorReturns(Location.builder().lat(41.889973).lng(-87.634024).name("Michigan and Ohio").build())
            .match();
    assertNotNull(match);
    assertEquals(Confidence.MEDIUM, match.getConfidence());
    assertEquals("Michigan and Ohio", match.getStop().getLocation().getName());
    verifyAll();
  }

  @Test
  public void testMatchThatIsGreaterThan50MilesAway_shouldFail() {
    TruckStopMatch match =
        tweet("Oh yea oh yea beautiful night in the Chi. " +
            "Come get ur froyo fix we are on the corner of Michigan and Ohio!")
            .geolocatorReturns(Location.builder().lat(41.889973).lng(80.634024).name("Michigan and Ohio").build())
            .match();
    assertNull(match);
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
    assertEquals(Confidence.MEDIUM, match.getConfidence());
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
    assertEquals(Confidence.MEDIUM, match.getConfidence());
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
    assertEquals(Confidence.MEDIUM, match.getConfidence());
    assertEquals(match.getStop().getStartTime(), tweetTime.withTime(11, 30, 0, 0));
  }

  @Test
  public void testMatch_shouldNotDetectFutureLocationIfBreakfastTruck() {
    tweetTime = new DateTime(2011, 11, 12, 7, 0, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder().id("foobar").name("FOO").twitterHandle("bar")
        .categories(ImmutableSet.of("Breakfast")).build();
    TruckStopMatch match =
        tweet("BeaversDonuts: Good Morning! The window is open at Erie and Franklin in front " +
            "of @FlairTower222, come on over we are here till 9ish.")
            .withTruck(truck)
            .match();
    assertNotNull(match);
    assertEquals(Confidence.MEDIUM, match.getConfidence());
    assertEquals(match.getStop().getStartTime(), tweetTime);
  }


  @Test
  public void testMatch_shouldInterpretEndTimeAsMorning() {
    tweetTime = new DateTime(2011, 11, 12, 6, 45, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder().id("foobar").name("FOO").twitterHandle("bar")
        .categories(ImmutableSet.of("Breakfast", "Lunch")).build();
    TruckStopMatch match =
        tweet("The Roost is open for breakfast. Come and get it 600w'ers. Here till 8:30ish but don't sit on your hands, these biscuits move!!")
            .withTruck(truck)
            .match();
    assertNotNull(match);
    assertEquals(Confidence.MEDIUM, match.getConfidence());
    assertEquals(match.getStop().getStartTime(), tweetTime);
    assertEquals(match.getStop().getEndTime(), tweetTime.withHourOfDay(8).withMinuteOfHour(30));
  }

  @Test
  public void testMatch_shouldInterpretEndTimeAsMorning2() {
    tweetTime = new DateTime(2011, 11, 12, 6, 45, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder().id("foobar").name("FOO").twitterHandle("bar")
        .categories(ImmutableSet.of("Breakfast", "Lunch")).build();
    TruckStopMatch match =
        tweet("Something delicious is happening @ 600 W Chicago this fine mornin'. It's us making you a fluffy, buttermilk biscuit with fried chicken. Mmm.")
            .withTruck(truck)
            .match();
    assertNotNull(match);
    assertEquals(Confidence.LOW, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
    assertEquals(tweetTime.plusHours(2), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_shouldNotDetectFutureLocationIfBreakfastAndLunchTruckWithBreakfast() {
    tweetTime = new DateTime(2011, 11, 12, 7, 0, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder().id("foobar").name("FOO").twitterHandle("bar")
        .categories(ImmutableSet.of("Breakfast", "Lunch")).build();
    TruckStopMatch match =
        tweet("Landed for breakfast @uchiNOMgo  @chiftf_uchicago @UChicago @UChicagoMed @UChicagoMag")
            .withTruck(truck)
            .match();
    assertNotNull(match);
    assertEquals(Confidence.LOW, match.getConfidence());
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
  public void testMatch_shouldntMatchDayOfWeek4() {
    TruckStopMatch match =
        tweet("Breakfast Sandwich Satisfaction\n" +
            "\n" +
            "Th 7AM Monroe/Wacker \n" +
            "F 7AM 600WChicago \n" +
            "Sa 10AM NOSH \n" +
            "Su 9AM 3627 N Southport")
            .match();
    assertNull(match);
  }

  @Test
  public void testMatch_when1030AndLunchTruckExtendEndTimeToOne() {
    TruckStopMatch match =
        tweet("foo")
          .withTime(tweetTime.withTime(10, 30, 0, 0))
          .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Lunch")).build())
          .match();
    assertEquals(13, match.getStop().getEndTime().getHourOfDay());
    assertEquals(0, match.getStop().getEndTime().getMinuteOfHour());
  }

  @Test
  public void testMatch_shouldntMatchDayOfWeek3() {
    TruckStopMatch match =
        tweet("We are having maintenance done this week. We will be at U of C on Weds, " +
            "but that is it. See ya then! ")
            .match();
    assertNull(match);
  }

  @Test
  public void testMatch_shouldMatchStartTime() {
    TruckStopMatch match =
        tweet(
            "Changing things up today! Clinton & Lake be there at 11a.m.. Plenty of Spicy and Herb Chicken. See y'all soon!")
            .withTruck(truck)
            .match();
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getStop().getStartTime());
  }

  @Test
  public void testMatch_shouldMatchStartTimeETA() {
    TruckStopMatch match =
        tweet(
            "We are enroute to the Univ of Chicago with bacon filled chocolate covered waffle sticks and 6 pancake flavors! ETA 8:00 am")
            .withTruck(truck)
            .match();
    assertEquals(tweetTime.withTime(8, 0, 0, 0), match.getStop().getStartTime());
  }

  @Test
  public void testMatch_shouldMatchStartTime1() {
    TruckStopMatch match =
        tweet(
            "Changing things up today! Clinton & Lake be there at 11am. Plenty of Spicy and Herb Chicken. See y'all soon!")
            .withTruck(truck)
            .match();
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getStop().getStartTime());
  }

  @Test
  public void testMatch_shouldMatchStartTime2() {
    TruckStopMatch match =
        tweet(
            "Changing things up today! Clinton & Lake be there at 12:45. Plenty of Spicy and Herb Chicken. See y'all soon!")
            .withTruck(truck)
            .match();
    assertEquals(tweetTime.withTime(12, 45, 0, 0), match.getStop().getStartTime());
  }

  @Test
  public void testMatch_shouldMatchStartTime3() {
    TruckStopMatch match =
        tweet(
            "Changing things up today! Clinton & Lake be there at 1. Plenty of Spicy and Herb Chicken. See y'all soon!")
            .withTruck(truck)
            .match();
    assertEquals(tweetTime.withTime(13, 0, 0, 0), match.getStop().getStartTime());
  }

  @Test
  public void testMatch_shouldMatchEndTime() {
    TruckStopMatch match =
        tweet(
            "Going strong at 600 W Chicago. Still got Italian bakes sandwiches and spinach lasagna!! Be here till 1:45 or supplies last!")
            .withTruck(truck)
            .withTime(tweetTime.withTime(11, 45, 0, 0))
            .match();
    assertEquals(tweetTime.withTime(11, 45, 0, 0), match.getStop().getStartTime());
  }

  @Test
  public void testMatch_shouldntMatchAbbreviatedSchedule() {
    TruckStopMatch match =
        tweet("THE TRUCK:\n" +
            "M: 600 W Chicago " +
            "T: NBC Tower\n" +
            "W: Clark & Monroe\n" +
            "TH: Madison & Wacker + Montrose & Ravenswood (5pm)\n" +
            "F: Lake & Wabash").match();
    assertNull(match);
  }

  @Test
  public void testMatch_onTuesday() {
    TruckStopMatch match =
        tweet("Breakfast sandwich awesomeness meets @DarkMatter2521 bliss.\n" +
            "\n" +
            "Tu 7a AON\n" +
            "W 7a 600WChicago").match();
    assertNull(match);
  }

  @Test
  public void testMatch_shouldntMatchTodaysSchedule() {
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
    assertEquals(Confidence.LOW, match.getConfidence());
    assertEquals(match.getStop().getStartTime(), tweetTime.withTime(11, 30, 0, 0));
  }

  @Test
  public void testMatch_shouldOpenAt11() {
    tweetTime = new DateTime(2011, 11, 9, 9, 0, DateTimeZone.UTC);
    TruckStopMatch match =
        tweet("Come find us today for lunch at Clark & Monroe. Windows open at 11:00am!!!! ")
            .match();
    assertNotNull(match);
    assertEquals(Confidence.MEDIUM, match.getConfidence());
    assertEquals(match.getStop().getStartTime(), tweetTime.withTime(11, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldNotMatchDayOfWeekIfTomorrow() {
    tweetTime = new DateTime(2011, 11, 7, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("@5411empanadas ahhh no uofc tues?? I shall starve").match();
    assertNull(match);
  }

  @Test
  public void testMatch_shouldNotMatchDayOfWeekIfTomorrow2() {
    tweetTime = new DateTime(2011, 11, 7, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Merch Mart, heading your way tmw. Carrying spicy chicken, " +
        "brunswick stew (website for details), corn on cob, biscuits and cucumber coleslaw.")
        .match();
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
    assertEquals(Confidence.MEDIUM, match.getConfidence());
    assertEquals(tweetTime, match.getStop().getStartTime());
  }

  @Test
  public void testMatch_shouldNotMatchWhenRetweet() {
    assertNull(tweet("Mmmm... RT @theslideride we are on Clinton & Lake").noParse().match());
  }

  @Test
  public void testMatch_shouldNotMatchQuotedRetweet() {
    assertNull(tweet("Mmmm... RT \"@theslideride we are on Clinton & Lake\"").noParse().match());
  }

  @Test
  public void testMatch_shouldNotMatchWhenRetweetWithNoPreceedingText() {
    assertNull(tweet("RT @theslideride we are on Clinton & Lake").noParse().match());
  }

  @Test
  public void testMatch_shouldMatchCloseAt() {
    tweetTime = new DateTime(2011, 11, 7, 9, 0, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder(truck).categories(ImmutableSet.of("Breakfast")).build();
    TruckStopMatch match = tweet("We are open at 58th and Ellis, we close at 1:30 PM")
        .match();
    assertEquals(match.getStop().getStartTime(), tweetTime.withTime(9, 0, 0, 0));
    assertEquals(match.getStop().getEndTime(), tweetTime.withTime(13, 30, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchAMTimeWhenStartsAtAm() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder(truck).categories(ImmutableSet.of("Breakfast")).build();
    TruckStopMatch match = tweet("Truck is Open @600WestBuilding @chiftf_600w @GrouponChicago until 11, or visit our store @ChiFrenchMarket until 6! pic.twitter.com/TsNxVi2ZPm")
        .match();
    assertEquals(tweetTime.withTime(7, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getStop().getEndTime());
  }


  @Test
  public void testMatch_shouldMatchStartAndEndWhen11aTo1p() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Here we come!!! Lunch is served! 11a-1p, 600 W Chicago Ave.  #bbq # lunch # foodtruck @GrouponChicago @chifoodtruckz @chiftf_600w")
        .match();
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(13, 0, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_shoudMatchTimeWhenNoAmPm() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Pancake lovers! We'll be at 750 N Orleans today, approx 8:30 - 10am. Stop by! Flavors: Bacon Egg n Chs, Red Velvet, Cinnamon Roll!").match();
    assertEquals(tweetTime.withTime(8, 30, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(10, 0, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_shoudMatchTimeRange() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Hey #UChicago....we are parked at 58th & Ellis for lunch today from 11:00am-2:00pm.   See ya'll soon.").match();
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(14, 0, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_shouldMatchLunchHour() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Here we come!!! Lunch is served! 11-1 600 W Chicago Ave.  #bbq # lunch # foodtruck @GrouponChicago @chifoodtruckz @chiftf_600w")
        .match();
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(13, 0, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_shouldMatchLunchHour2() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("\n" +
        "Jerk. is at Wacker/Madison from 11-2p #lunch #chicago #foodtruck #jamaican #jerkchicken #bbq http://t.co/LdzNBUJXJu")
        .match();
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(14, 0, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_shouldMatchLunchHour3() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Lake and LaSalle 12-2 Northwestern Campus 3-7")
        .match();
    assertEquals(tweetTime.withTime(12, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(14, 0, 0, 0), match.getStop().getEndTime());
  }

  @Test
  public void testMatch_lunchAnd11a() {
    truck = Truck.builder(truck)
        .categories(ImmutableSet.of("Lunch")).build();
    tweetTime = tweetTime.withHourOfDay(7);
    TruckStopMatch match = tweet("\n" +
        "Jerk. is at Madison/Wacker today 11a #jerkchicken #foodtruck #jamaican #chicago #lunch #foodie http://t.co/jWVAaz2BxR\n" +
        "2/19/14 9:05 AM")
        .match();
    assertEquals(tweetTime.withTime(11, 0, 0, 0), match.getStop().getStartTime());
    assertEquals(tweetTime.withTime(13, 0, 0, 0), match.getStop().getEndTime());
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
      this.geolocatorResult = Location.builder().lat(41.889973).lng(-87.634024).name(address).build();
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
          .userId("foobar")
          .time(Tweeter.this.time).build();
      return topic.match(Tweeter.this.truck, tweet, null);
    }

    public Tweeter noParse() {
      expectParse = false;
      return this;
    }
  }

}
