package foodtruck.schedule;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.time.DayOfWeek;
import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.notifications.SystemNotificationService;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;
import static org.easymock.EasyMock.expect;

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
  private SystemNotificationService notifier;

  @Before
  public void before() {
    extractor = createMock(AddressExtractor.class);
    geolocator = createMock(GeoLocator.class);
    clock = createMock(Clock.class);
    notifier = createMock(SystemNotificationService.class);
    Location mapCenter = Location.builder().lat(41.8807438).lng(-87.6293867).build();
    expect(clock.dayOfWeek()).andStubReturn(DayOfWeek.sunday);
    topic = new TruckStopMatcher(extractor, geolocator, DateTimeZone.UTC, clock, notifier, mapCenter,
        new LocalTime(11, 30), ImmutableMap.<String, SpecialMatcher>of());
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
  public void testMatch_removePhoneNumbers() {
    TruckStopMatch match = tweet("Lunch 48th bet 6 and 7\n" +
        "Ave\n" +
        "\n" +
        "DISOS ROAST BEEF SPECIAL TODAY!!\n" +
        "\n" +
        "Call in orders to: 917-756-4145").withTime(tweetTime)
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.plusHours(2));

  }

  @Test
  public void testMatch_whenHardEndWithSoftStart() {
    truck = Truck.builder().id("foobar").name("FOO").twitterHandle("bar")
        .categories(ImmutableSet.of("Breakfast", "Lunch")).build();
    tweetTime = tweetTime.withTime(7, 0, 0, 0);
    TruckStopMatch match =
        tweet("TheRoostTruck: Rise and shine, folks. The Roost is serving up the best damn chicken biscuits on wheels over at Madisin and Wacker till 9:00am.")
            .withTime(tweetTime)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(9, 0, 0, 0));
  }

  @Test
  public void testMatch_biscuit() {
    tweetTime = tweetTime.withTime(7, 0, 0, 0);
    truck = Truck.builder().id("foobar").name("FOO").twitterHandle("bar")
        .categories(ImmutableSet.of("Breakfast", "Lunch")).build();
    TruckStopMatch match =
        tweet("The Roost is serving up the best damn chicken biscuits on wheels over at Madisin and Wacker.")
            .withTime(tweetTime)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(9, 0, 0, 0));
  }

  @Test
  public void testMatch_riseAndShine() {
    tweetTime = tweetTime.withTime(7, 0, 0, 0);
    truck = Truck.builder().id("foobar").name("FOO").twitterHandle("bar")
        .categories(ImmutableSet.of("Breakfast", "Lunch")).build();
    TruckStopMatch match =
        tweet("TheRoostTruck: Rise and shine, folks. The Roost is serving up the best damn chicken  on wheels over at Madisin and Wacker.")
            .withTime(tweetTime)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(9, 0, 0, 0));
  }

  @Test
  public void testMatch1100230() {
    tweetTime = tweetTime.withTime(10, 42, 0, 0);
    truck = Truck.builder()
        .id("foobar")
        .name("FOO")
        .twitterHandle("bar")
        .categories(ImmutableSet.of("Lunch"))
        .build();
    TruckStopMatch match = tweet(
        "Come Out And Have Lunch With Us Between 11Am-2:30Pm On Varick St And Vandam St").withTime(tweetTime)
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(14, 30, 0, 0));

  }

  @Test
  public void testMatch_1130130() {
    tweetTime = tweetTime.withTime(10, 42, 0, 0);
    truck = Truck.builder()
        .id("foobar")
        .name("FOO")
        .twitterHandle("bar")
        .categories(ImmutableSet.of("Lunch"))
        .build();
    TruckStopMatch match = tweet("Back on WACKER & ADAMS today! Serving 11:30-1:30pm!").withTime(tweetTime)
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(11, 30, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(13, 30, 0, 0));
  }

  @Test
  public void testMatch_morningTweet1230() {
    tweetTime = tweetTime.withTime(7, 0, 0, 0);
    truck = Truck.builder().id("foobar").name("FOO").twitterHandle("bar")
        .categories(ImmutableSet.of("Breakfast")).build();
    TruckStopMatch match =
        tweet("We're in the West Loop at Sangamon and Monroe until 12:30!")
            .withTime(tweetTime)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(12, 30, 0, 0));
  }

  @Test
  public void testMatch_carriageReturn() {
    assertThat(tweet(
        "1815 S. Meyers Road, Chicago, IL] from tweet: Corporate Lakes III, 2200 Cabot Drive, Lisle 1:30-3\n" +
            "\n" +
            "Oakbrook Terrace Corporate Center III, 1815 S. Meyers Road 3:15-4:30")
        .match()).isNotNull();
  }

  @Test
  public void testMatch_shouldReturnNullWhenNoAddress() {
    final String tweetText = "foobar";
    expect(extractor.parse(tweetText, truck)).andReturn(ImmutableList.<String>of());
    replayAll();
    Story tweet = new Story.Builder().text(tweetText).time(tweetTime).build();
    assertThat(topic.match(truck, tweet)).isNull();
  }

  @Test
  public void testMatch_shouldReturnNullWhenUnableToGeolocate() {
    assertThat(tweet("Culture: Last call Erie and Kingsbury, outta here in 15 minutes, " +
            "then off to our next River North location, Hubbard & LaSalle")
            .geolocatorReturns(null)
            .match()).isNull();
  }

  @Test
  public void testMatch_shouldNotifyByEmailWhenNewLocation() {
    String tweet = "Last call Erie and Kingsbury, outta here in 15 minutes," +
        " then off to our next River North location, Hubbard & LaSalle";
    Location location =Location.builder().lat(41.889973).lng(-87.634024).name("foobar")
        .wasJustResolved(true).build();
    notifier.systemNotifyLocationAdded(location,
        new Story.Builder().userId("foobar").text(tweet).time(tweetTime).build(), truck);
    TruckStopMatch match =
        tweet(tweet)
            .geolocatorReturns(location)
            .match();
    assertThat(match).isNotNull();
  }

  @Test
  public void testMatch_breakfastUntil() {
    tweetTime = tweetTime.withTime(8, 0, 0, 0);
    truck = Truck.builder()
        .categories(ImmutableSet.of("Breakfast", "Lunch"))
        .build();
    TruckStopMatch match = tweet("Breakfast is on... 9am till 11am.@chiftf_uchicago@uchiNOMgo").withTruck(truck)
        .withTime(tweetTime)
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(9, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));

  }

  @Test
  public void testMatch_shouldReturnHighConfidenceWhenAtLocationUntil() {
    TruckStopMatch match =
        tweet("Gold Coast, we have landed at Rush and Walton...here until 6 pm!")
            .withTime(tweetTime)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldReturnHighConfidenceWhenLeavingAt() {
    tweetTime = tweetTime.withHourOfDay(16).withMinuteOfHour(16);
    TruckStopMatch match =
        tweet("Our final stop is Grand & McClurg. We'll be leaving at 5:30pm, so come soon!")
            .withTime(tweetTime)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getNotes()).contains(
        "Tweet received for location: 'Our final stop is Grand & McClurg. We'll be leaving at 5:30pm, so come soon!'");
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(17, 30, 0, 0));
  }

  @Test
  public void testMatch_lineBreak() {
    tweetTime = tweetTime.withHourOfDay(16).withMinuteOfHour(16);
    TruckStopMatch match =
        tweet("Selling at 1623 N Damen - Timbuk2!\n" +
            "\n" +
            "Until 8\n" +
            "\n" +
            "#GratefulChacos\n" +
            "\n")
            .withTime(tweetTime)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(20, 0, 0, 0));
  }

  @Test
  public void testMatch_lineBreak2() {
    tweetTime = tweetTime.withHourOfDay(8).withMinuteOfHour(16);
    TruckStopMatch match =
        tweet("Check us out at the Chago Dept of Aviation \n" +
            "Food Truck Fest\n" +
            "10A -2P\n" +
            "10510 W Zemke Blvd")
            .withTime(tweetTime)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(10, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
  }

  @Test
  public void testMatch_latetNight2() {
    tweetTime = tweetTime.withHourOfDay(0).withMinuteOfHour(37);
    TruckStopMatch match =
        tweet("late Late Dinner 680 N Franklin 12:00am-2:00am @chifoodtruckz @YBar @Spybar @CuveeChicago #LateNight ")
            .withTime(tweetTime)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(0, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(2, 0, 0, 0));
  }

  @Test
  public void testMatch_latetNight3() {
    tweetTime = tweetTime.withHourOfDay(0).withMinuteOfHour(37);
    TruckStopMatch match =
        tweet("late Late Dinner 680 N Franklin until 2:00am @chifoodtruckz @YBar @Spybar @CuveeChicago #LateNight ")
            .withTime(tweetTime)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(2, 0, 0, 0));
  }

  @Test
  public void testMatch_specifiedEndTime() {
    TruckStopMatch match =
        tweet(
            "SAY HEY CHICAAAAGO @ CLARK & MONROE FROM 11-2PM! OUR BIRMINGHAM BLACK BARONS' PEACH COBBLER IS ON DECK TODAY!... http://t.co/4vXlQtKBqp")
            .withTime(tweetTime.withTime(7, 0, 0, 0))
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
  }

  @Test
  public void testMatch_tonightHandling() {
    tweetTime = tweetTime.withHourOfDay(13).withMinuteOfHour(16);
    truck = Truck.builder().categories(ImmutableSet.of("Breakfast", "MorningSquatter", "HasNightStops")).build();
    TruckStopMatch match =
        tweet("#Vaultvan needs a break! See you tonight at #greenstreetsmokedmeats")
            .withTime(tweetTime)
            .withTruck(truck)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(17, 30, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(19, 30, 0, 0));
  }

  @Test
  public void testWithReturn() {
    TruckStopMatch match = tweet("We are now open for business at 600 west Chicago! We will be serving until 8:30\n" +
        "\n" +
        "@600WestBuilding @Groupon @BellyChicago @BarryCallebaut")
        .withTime(tweetTime.withTime(7, 0, 0, 0))
        .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Breakfast")).build())
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(8, 30, 0, 0));
  }

  @Test
  public void testMorningRange() {
    TruckStopMatch match = tweet("Get ready, Jackson and Wabash! We're coming your way this AM--7 - 10. Let us make your morning better, won't you?")
        .withTime(tweetTime.withTime(6, 58, 0, 0))
        .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Lunch", "Breakfast")).build())
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(10, 0, 0, 0));
  }

  @Test
  public void testNightTimeRange() {
    TruckStopMatch match = tweet("Come check us out at Lake & Michigan 9-11 tonight! ")
        .withTime(tweetTime.withTime(20, 43, 0, 0))
        .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Lunch")).build())
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(21, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(23, 0, 0, 0));
  }

  @Test
  public void testNightTimeRange2() {
    TruckStopMatch match = tweet("Come check us out at Lake & Michigan 6:45pm-7:30pm")
        .withTime(tweetTime.withTime(7, 43, 0, 0))
        .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Breakfast")).build())
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(18, 45, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(19, 30, 0, 0));
  }

  @Test
  public void testNightTimeRangeWithDayBoundary() {
    TruckStopMatch match = tweet("Serving steak & cheese at 1030-1230 draft bar 8221 w Irving park rd")
        .withTime(tweetTime.withTime(22, 10, 0, 0))
        .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Lunch")).build())
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(22, 30, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(match.getStop().getStartTime().plusHours(2));
  }

  @Test
  public void testNightTimeRangeWithDayBoundary2() {
    TruckStopMatch match = tweet("Serving steak & cheese at 1030-130 draft bar 8221 w Irving park rd")
        .withTime(tweetTime.withTime(22, 10, 0, 0))
        .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Lunch")).build())
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(22, 30, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(match.getStop().getStartTime().plusHours(3));
  }

  @Test
  public void testWith12() {
    TruckStopMatch match = tweet("Lake and LaSalle 12-2 Northwestern Campus 3-7")
        .withTime(tweetTime.withTime(7, 0, 0, 0))
        .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Cupcakes")).build())
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(12, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
  }

  @Test
  public void testWithLaterRange() {
    TruckStopMatch match = tweet("Come to Walton and Dearborn for a Steak & Cheese! 815- 930 ")
        .withTime(tweetTime.withTime(20, 18, 0, 0))
        .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Cupcakes")).build())
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(20, 15, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(21, 30, 0, 0));
  }


  @Test
  public void testMatch_shouldReturnHighConfidenceWhenAtLocationTil() {
    TruckStopMatch match = tweet("Gold Coast, we have landed at Rush and Walton...here til 6 pm!")
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
  }

  // til autocorrects to till so handle that too
  @Test
  public void testMatch_shouldReturnHighConfidenceWhenAtLocationTill() {
    TruckStopMatch match = tweet("Gold Coast, we have landed at Rush and Walton...here till 6 pm!")
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(18, 0, 0, 0));
  }

  @Test
  public void testMatch_anotherUntil() {
    TruckStopMatch match = tweet("At 353 N Desplaines until 8pm with @bigstarchicago  !!")
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(20, 0, 0, 0));
  }

  @Test
  @Ignore
  public void testMatch_yetAnotherUntil() {
    TruckStopMatch match = tweet(
        "Look For The Truck @UChicago This Afternoon Till 3 PM On Ellis! http://t.co/TD1aZ8OyHt")
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(15, 0, 0, 0));
  }


  @Test
  public void testMatch_includesCurrentDayOfTheWeek() {
    TruckStopMatch match = tweet(
        "SweetSpotMac: Arrived at Michigan and Walton. " + "Come get your Sunday macaron going!")
        .match();

    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(13, 13, 7, 7));
  }

  @Test
  public void testMatch_shouldReturnMatch() {
    truck = Truck.builder(truck).categories(ImmutableSet.of("Dessert")).build();
    TruckStopMatch match =
        tweet("Oh yea oh yea beautiful night in the Chi. " +
            "Come get ur froyo fix we are on the corner of Michigan and Ohio!")
            .geolocatorReturns(Location.builder().lat(41.889973).lng(-87.634024).name("Michigan and Ohio").build())
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getLocation().getName()).isEqualTo("Michigan and Ohio");
    verifyAll();
  }

  @Test
  public void testMatch_matchExtendedTime() {
    truck = Truck.builder(truck).categories(ImmutableSet.of("Breakfast", "MorningSquatter")).build();
    tweetTime = tweetTime.withTime(7, 5, 0, 0);
    TruckStopMatch match =
        tweet("It's a beautiful day for some food truck grub. Breakfast and Lunch at 125 S. Clark! Get hungry for Poblano  Chicken ")
            .geolocatorReturns(Location.builder().lat(41.889973).lng(-87.634024).name("Michigan and Ohio").build())
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
    assertThat(match.getStop().getLocation().getName()).isEqualTo("Michigan and Ohio");
    verifyAll();
  }

  @Test
  public void testMatch_matchExtendedTime2() {
    truck = Truck.builder(truck).categories(ImmutableSet.of("Breakfast", "MorningSquatter")).build();
    tweetTime = tweetTime.withTime(7, 5, 0, 0);
    TruckStopMatch match =
        tweet("CocinitaChicago: Serving Breakfast Tacos and then Lunch at Adams & Wacker! http://t.co/5Nyq0VX0jI")
            .geolocatorReturns(Location.builder().lat(41.889973).lng(-87.634024).name("Michigan and Ohio").build())
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
    assertThat(match.getStop().getLocation().getName()).isEqualTo("Michigan and Ohio");
    verifyAll();
  }


  @Test
  public void testMatchThatIsGreaterThan50MilesAway_shouldFail() {
    TruckStopMatch match =
        tweet("Oh yea oh yea beautiful night in the Chi. " +
            "Come get ur froyo fix we are on the corner of Michigan and Ohio!")
            .geolocatorReturns(Location.builder().lat(41.889973).lng(80.634024).name("Michigan and Ohio").build())
            .match();
    assertThat(match).isNull();
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
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(13, 30, 0, 0));
    assertThat(match.getStop().getLocation().getName()).isEqualTo("Foo and Bar");
  }

  @Test
  public void testMatch_shouldDetectTimeRangeNoon() {
    tweetTime = new DateTime(2011, 11, 12, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("The tamalespaceship will be landing at our weekly spot " +
        "<<Dearborn & Monroe>> 11a.m.-noon. last chance to get your tamale fix before the weekend!!")
        .withTime(tweetTime)
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(12, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldDetectFutureLocation() {
    tweetTime = new DateTime(2011, 11, 12, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet(
        "Rush & UIC Medical Center DonRafa is gonna be in ur area today!" + " Don't want to come out? call 312-498-9286 we... fb.me/1gKduQrvS")
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 30, 0, 0));
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
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
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
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo( tweetTime.withHourOfDay(8).withMinuteOfHour(30));
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
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(9, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldInterpretStartTimeAsMorning() {
    tweetTime = new DateTime(2011, 11, 12, 6, 45, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder().id("foobar").name("FOO").twitterHandle("bar")
        .categories(ImmutableSet.of("Breakfast", "Lunch")).build();
    TruckStopMatch match =
        tweet("Serving brunch at Southport & Addison until 1 PM!")
            .withTruck(truck)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(13, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldInterpretStartTimeAsMorning2() {
    tweetTime = new DateTime(2011, 11, 12, 6, 45, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder().id("foobar").name("FOO").twitterHandle("bar")
        .categories(ImmutableSet.of("Breakfast", "Lunch")).build();
    TruckStopMatch match =
        tweet("Check out the truck for b'fast at Clark & Monroe and you might receive total consciousness.So you'll have that goin for ya, which is nice.")
            .withTruck(truck)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(9, 0, 0, 0));
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
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
  }

  // for now, we can't handle tweets like this.
  @Test
  public void testMatch_shouldntMatchDayOfWeek() {
    assertThat(tweet("5411empanadas: MON: Oak and Michigan / TUE: Univ of Chicago (Hyde Park) " +
            "/ WED: Dearborn & Monroe / THU: Columbus & Randolph / FRI: Wacker & Van Buren")
            .noParse()
            .match()).isNull();
  }

  @Test
  public void testMatch_shouldntMatchDayOfWeek4() {
    assertThat(tweet("Breakfast Sandwich Satisfaction\n" +
            "\n" +
            "Th 7AM Monroe/Wacker \n" +
            "F 7AM 600WChicago \n" +
            "Sa 10AM NOSH \n" +
            "Su 9AM 3627 N Southport")
            .noParse()
            .match()).isNull();
  }

  @Test
  public void testHandleFirstTime() {
    TruckStopMatch match =
        tweet("Lil Red’s spots\n" +
            "Clark&monroe8:30-2\n" +
            "Lasalle&adams2:15-3\n" +
            "Wabash&jackson3:30-4:45\n" +
            "Vanburen&wabash4:50-6\n" +
            "Kingsbury&Erie6:15-7:30").match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(8, 30, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
  }

  @Test
  public void testHandleShortenedRange() {
    TruckStopMatch match = tweet(
        "We're at 3710 N. Southport ready for Trick or Treat on Southport from 4pm-8pm! ").match();
    assertThat(match).isNotNull();
    assertThat(match.getStop()
        .getStartTime()).isEqualTo(tweetTime.withTime(16, 0, 0, 0));
    assertThat(match.getStop()
        .getEndTime()).isEqualTo(tweetTime.withTime(20, 0, 0, 0));

  }

  @Test
  public void testMatch_when1030AndLunchTruckExtendEndTimeToTwo() {
    TruckStopMatch match =
        tweet("foo")
            .withTime(tweetTime.withTime(10, 30, 0, 0))
            .withTruck(Truck.builder(truck).categories(ImmutableSet.of("Lunch")).build())
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldntMatchDayOfWeek3() {
    assertThat(tweet(
            "We are having maintenance done this week. We will be at U of C on Weds, " + "but that is it. See ya then! ")
            .noParse()
            .match()).isNull();
  }

  @Test
  public void testMatch_shouldMatchStartTime() {
    TruckStopMatch match =
        tweet(
            "Changing things up today! Clinton & Lake be there at 11a.m.. Plenty of Spicy and Herb Chicken. See y'all soon!")
            .withTruck(truck)
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchStartTime4() {
    truck = Truck.builder(truck)
        .categories(ImmutableSet.of("Lunch")).build();
    TruckStopMatch match =
        tweet(
            "Happy Regular:) Join us at 11AM today on Franklin and Adams. @chifoodtruckz. #foodcart #crepes")
            .withTruck(truck)
            .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchStartTimeETA() {
    TruckStopMatch match =
        tweet(
            "We are enroute to the Univ of Chicago with bacon filled chocolate covered waffle sticks and 6 pancake flavors! ETA 8:00 am")
            .withTruck(truck)
            .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(8, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchStartTime1() {
    TruckStopMatch match =
        tweet(
            "Changing things up today! Clinton & Lake be there at 11am. Plenty of Spicy and Herb Chicken. See y'all soon!")
            .withTruck(truck)
            .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchStartTime5() {
    TruckStopMatch match =
        tweet(
            "***Bark Travel News*** Tonight at 4pm we'll be serving up treats at River North Park Apartments -... ")
            .withTruck(truck)
            .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(16, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchStartTime2() {
    TruckStopMatch match =
        tweet(
            "Changing things up today! Clinton & Lake be there at 12:45. Plenty of Spicy and Herb Chicken. See y'all soon!")
            .withTruck(truck)
            .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(12, 45, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchStartTime3() {
    TruckStopMatch match =
        tweet(
            "Changing things up today! Clinton & Lake be there at 1. Plenty of Spicy and Herb Chicken. See y'all soon!")
            .withTruck(truck)
            .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(13, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchEndTime() {
    TruckStopMatch match =
        tweet(
            "Going strong at 600 W Chicago. Still got Italian bakes sandwiches and spinach lasagna!! Be here till 1:45 or supplies last!")
            .withTruck(truck)
            .withTime(tweetTime.withTime(11, 45, 0, 0))
            .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 45, 0, 0));
  }

  @Test
  public void testMatch_shouldntMatchAbbreviatedSchedule() {
    TruckStopMatch match =
        tweet("THE TRUCK:\n" +
            "M: 600 W Chicago " +
            "T: NBC Tower\n" +
            "W: Clark & Monroe\n" +
            "TH: Madison & Wacker + Montrose & Ravenswood (5pm)\n" +
            "F: Lake & Wabash")
            .noParse()
            .match();
    assertThat(match).isNull();
  }

  @Test
  public void testMatch_onTuesday() {
    TruckStopMatch match =
        tweet("Breakfast sandwich awesomeness meets @DarkMatter2521 bliss.\n" +
            "\n" +
            "Tu 7a AON\n" +
            "W 7a 600WChicago")
            .noParse()
            .match();
    assertThat(match).isNull();
  }

  @Test
  public void testMatch_shouldntMatchTodaysSchedule() {
    TruckStopMatch match = tweet("SweetRideChi: TUES STOPS:  1130a - Taylor & Wood\n" +
        "1245p - UIC Campus Vernon Park Circle by BSB bldg\n" +
        "245p - Wacker & Lasalle\n" +
        "430p... http://t.co/EDVtU2XM")
        .noParse()
        .match();
    assertThat(match).isNull();
  }

  // for now, we can't handle tweets like this.
  @Test
  public void testMatch_shouldntMatchDayOfWeek2() {
    expect(clock.dayOfWeek()).andReturn(DayOfWeek.sunday);
    tweetTime = new DateTime(2011, 11, 12, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match =
        tweet("We hope you having a great weekend, see you on Monday <<Wells & Monroe>>")
            .noParse()
            .match();
    assertThat(match).isNull();
  }

  @Test
  public void testMatch_shouldntMatchDate() {
    tweetTime = new DateTime(2011, 11, 12, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match =
        tweet("MonstaLobsta: 1/29 Lunch 11 til 1:30p at Bank of America 390 N Orange Ave. Let's Roll!!!")
            .noParse()
            .match();
    assertThat(match).isNull();
  }

  @Test
  public void testMatch_shouldMatchDate() {
    tweetTime = new DateTime(2015, 1, 29, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match =
        tweet("MonstaLobsta: 1/29 Lunch 11 til 1:30p at Bank of America 390 N Orange Ave. Let's Roll!!!")
            .noParse()
            .match();
    assertThat(match).isNull();
  }

  @Test
  public void testMatch_shouldMatchDayOfWeekIfCurrentDay() {
    tweetTime = new DateTime(2011, 11, 6, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match =
        tweet(
            "GiGisBakeShop: Hello SUNDAY!  The PURPLE Bus is headed out...Look for us at " + "13th / S Michigan 11:15 am, Lincoln Square 1:30 pm")
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 30, 0, 0));
  }

  @Test
  public void testMatch_shouldOpenAt11() {
    tweetTime = new DateTime(2011, 11, 9, 9, 0, DateTimeZone.UTC);
    TruckStopMatch match =
        tweet("Come find us today for lunch at Clark & Monroe. Windows open at 11:00am!!!! ")
            .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldNotMatchDayOfWeekIfTomorrow() {
    tweetTime = new DateTime(2011, 11, 7, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("@5411empanadas ahhh no uofc tues?? I shall starve")
        .noParse()
        .match();
    assertThat(match).isNull();
  }

  @Test
  public void testMatch_shouldNotMatchDayOfWeekIfTomorrow2() {
    tweetTime = new DateTime(2011, 11, 7, 9, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Merch Mart, heading your way tmw. Carrying spicy chicken, " +
        "brunswick stew (website for details), corn on cob, biscuits and cucumber coleslaw.")
        .noParse()
        .match();
    assertThat(match).isNull();
  }

  @Test
  public void testMatch_shouldNotMatchWhenHashNotPresent() {
    truck = new Truck.Builder(truck).matchOnlyIf("#bunsontherun").build();
    TruckStopMatch match = tweet(
        "Oooops on the handshake between Chris and Taylor - next time try a fistbump #AMA2011")
        .withTruck(truck)
        .noParse()
        .match();
    assertThat(match).isNull();
    verifyAll();
  }

  @Test
  public void testMatch_shouldMatchWhenHasMatchOnlyIfExpression() {
    truck = new Truck.Builder(truck).matchOnlyIf("#bunsontherun").build();
    TruckStopMatch match = tweet(
        "Oooops on the handshake between Chris and Taylor - next time try a fistbump #BunsOnTheRun")
        .withTruck(truck)
        .match();
    assertThat(match).isNotNull();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime);
  }

  @Test
  public void testMatch_shouldNotMatchWhenRetweet() {
    assertThat(tweet("Mmmm... RT @theslideride we are on Clinton & Lake").noParse().match()).isNull();
  }

  @Test
  public void testMatch_shouldNotMatchQuotedRetweet() {
    assertThat(tweet("Mmmm... RT \"@theslideride we are on Clinton & Lake\"").noParse().match()).isNull();
  }

  @Test
  public void testMatch_shouldNotMatchWhenRetweetWithNoPreceedingText() {
    assertThat(tweet("RT @theslideride we are on Clinton & Lake").noParse().match()).isNull();
  }

  @Test
  public void testMatch_shouldMatchCloseAt() {
    tweetTime = new DateTime(2011, 11, 7, 9, 0, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder(truck).categories(ImmutableSet.of("Breakfast")).build();
    TruckStopMatch match = tweet("We are open at 58th and Ellis, we close at 1:30 PM")
        .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(9, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(13, 30, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchAMTimeWhenStartsAtAm() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    truck = Truck.builder(truck).categories(ImmutableSet.of("Breakfast")).build();
    TruckStopMatch match = tweet("Truck is Open @600WestBuilding @chiftf_600w @GrouponChicago until 11, or visit our store @ChiFrenchMarket until 6! pic.twitter.com/TsNxVi2ZPm")
        .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(7, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchStartAndEndWhen11aTo1p() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Here we come!!! Lunch is served! 11a-1p, 600 W Chicago Ave.  #bbq # lunch # foodtruck @GrouponChicago @chifoodtruckz @chiftf_600w")
        .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(13, 0, 0, 0));
  }

  @Test
  public void testMatch_shoudMatchTimeWhenNoAmPm() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Pancake lovers! We'll be at 750 N Orleans today, approx 8:30 - 10am. Stop by! Flavors: Bacon Egg n Chs, Red Velvet, Cinnamon Roll!").match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(8, 30, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(10, 0, 0, 0));
  }

  @Test
  public void testMatch_shoudMatchTimeRange() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Hey #UChicago....we are parked at 58th & Ellis for lunch today from 11:00am-2:00pm.   See ya'll soon.").match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchLunchHour() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Here we come!!! Lunch is served! 11-1 600 W Chicago Ave.  #bbq # lunch # foodtruck @GrouponChicago @chifoodtruckz @chiftf_600w")
        .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(13, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchLunchHour2() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("\n" +
        "Jerk. is at Wacker/Madison from 11-2p #lunch #chicago #foodtruck #jamaican #jerkchicken #bbq http://t.co/LdzNBUJXJu")
        .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchLunchHour3() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("Lake and LaSalle 12-2 Northwestern Campus 3-7")
        .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(12, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
  }

  @Test
  public void testMatch_shouldMatchLunchHour4() {
    tweetTime = new DateTime(2011, 11, 7, 7, 0, 0, 0, DateTimeZone.UTC);
    TruckStopMatch match = tweet("\n" +
        "Lunch today at @ShopNorthBridge for #FoodTruckWednesdays! 443 N Wabash, 11 AM-2 PM w/ @DaLobstaChicago &@GenosSteaks http://t.co/9VhW52nJg2")
        .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(14, 0, 0, 0));
  }

  @Test
  public void testMatch_lunchAnd11a() {
    truck = Truck.builder(truck)
        .categories(ImmutableSet.of("Lunch")).build();
    tweetTime = tweetTime.withHourOfDay(7);
    TruckStopMatch match = tweet("\n" +
        "Jerk. is at Madison/Wacker today 11a #jerkchicken #foodtruck #jamaican #chicago #lunch #foodie http://t.co/jWVAaz2BxR\n" +
        " 9:05 AM")
        .match();
    assertThat(match.getStop().getStartTime()).isEqualTo(tweetTime.withTime(11, 0, 0, 0));
    assertThat(match.getStop().getEndTime()).isEqualTo(tweetTime.withTime(13, 0, 0, 0));
  }

  private Tweeter tweet(String tweet) {
    return new Tweeter(tweet);
  }

  private class Tweeter {
    private String tweet;
    private Truck truck;
    private DateTime time;
    private String address = "Foo and Bar";
    private Location geolocatorResult;
    private boolean expectParse = true;

    Tweeter(String tweet) {
      Tweeter.this.tweet = tweet;
      Tweeter.this.truck = TruckStopMatcherTest.this.truck;
      Tweeter.this.time = TruckStopMatcherTest.this.tweetTime;
      this.geolocatorResult = Location.builder().lat(41.889973).lng(-87.634024).name(address).build();
    }

    Tweeter withTruck(Truck truck) {
      Tweeter.this.truck = truck;
      return this;
    }

    Tweeter withTime(DateTime time) {
      this.time = time;
      return this;
    }

    Tweeter geolocatorReturns(@Nullable Location location) {
      this.geolocatorResult = location;
      return this;
    }

    TruckStopMatch match() {
      if (expectParse) {
        expect(extractor.parse(tweet, Tweeter.this.truck)).andReturn(ImmutableList.of(address));
        expect(geolocator.locate(address, GeolocationGranularity.NARROW))
            .andReturn(geolocatorResult);
      }
      replayAll();
      Story tweet = new Story.Builder().text(Tweeter.this.tweet)
          .userId("foobar")
          .time(Tweeter.this.time).build();
      return topic.match(Tweeter.this.truck, tweet);
    }

    Tweeter noParse() {
      expectParse = false;
      return this;
    }
  }
}
