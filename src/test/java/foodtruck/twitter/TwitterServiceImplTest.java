package foodtruck.twitter;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TweetCacheDAO;
import foodtruck.email.EmailNotifier;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.model.TweetSummary;
import foodtruck.schedule.OffTheRoadDetector;
import foodtruck.schedule.TerminationDetector;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.TruckStopMatcher;
import foodtruck.truckstops.LoggingTruckStopNotifier;
import foodtruck.util.Clock;
import static org.easymock.EasyMock.expect;

/**
 * @author aviolette@gmail.com
 * @since 10/18/11
 */
public class TwitterServiceImplTest extends EasyMockSupport {

  private TweetCacheDAO tweetDAO;
  private TruckStopMatcher matcher;
  private TruckStopDAO truckStopDAO;
  private TwitterServiceImpl service;
  private static final String TRUCK_1_ID = "truck1";
  private static final String TRUCK_2_ID = "truck2";
  private Truck truck2;
  private DateTime now;
  private LocalDate currentDay;
  private TerminationDetector terminationDetector;
  private Location loca;
  private Location locb;
  private TruckStop matchedStop;
  private DateTime matchStartTime;
  private DateTime matchEndTime;
  private TruckDAO truckDAO;
  private TweetSummary basicTweet;
  private EmailNotifier emailNotifier;
  private OffTheRoadDetector offTheRoadDetector;
  private GeoLocator locator;
  private Location uofc;
  private Truck truck1;

  @Before
  public void before() {
    final TwitterFactoryWrapper twitterFactory = createMock(TwitterFactoryWrapper.class);
    tweetDAO = createMock(TweetCacheDAO.class);
    truck1 = new Truck.Builder().id(TRUCK_1_ID).twitterHandle(TRUCK_1_ID)
        .useTwittalyzer(false).build();
    truck2 = new Truck.Builder().id(TRUCK_2_ID).twitterHandle(TRUCK_2_ID)
        .useTwittalyzer(true).build();
    final DateTimeZone zone = DateTimeZone.forID("America/Chicago");
    emailNotifier = createMock(EmailNotifier.class);
    matcher = createMock(TruckStopMatcher.class);
    truckStopDAO = createMock(TruckStopDAO.class);
    locator = createMock(GeoLocator.class);
    final Clock clock = createMock(Clock.class);
    now = new DateTime(2011, 10, 9, 8, 0, 0, 0, zone);
    currentDay = now.toLocalDate();
    expect(clock.now()).andStubReturn(now);
    expect(clock.currentDay()).andStubReturn(currentDay);
    ImmutableMap<String, Truck> truckMap =
        ImmutableMap.of(TRUCK_1_ID, truck1, TRUCK_2_ID, truck2);
    truckDAO = createMock(TruckDAO.class);
    expect(truckDAO.findById(TRUCK_1_ID)).andStubReturn(truck1);
    expect(truckDAO.findById(TRUCK_1_ID)).andStubReturn(truck2);
    expect(truckDAO.findAll()).andStubReturn(ImmutableSet.of(truck2));
    final int listId = 123;
    terminationDetector = createMock(TerminationDetector.class);
    ConfigurationDAO configDAO = createMock(ConfigurationDAO.class);
    offTheRoadDetector = createMock(OffTheRoadDetector.class);
    service = new TwitterServiceImpl(twitterFactory, tweetDAO, zone, matcher,
        truckStopDAO,
        clock, terminationDetector, new LocalCacheUpdater(), truckDAO,
        new LoggingTruckStopNotifier(), configDAO, emailNotifier, offTheRoadDetector, locator);
    loca = Location.builder().lat(1).lng(2).name("a").build();
    locb = Location.builder().lat(3).lng(4).name("b").build();
    basicTweet = new TweetSummary.Builder().time(now.minusHours(2)).text(
        "We are at Kingsbury and Erie.").build();
    List<TweetSummary> tweets = ImmutableList.of(basicTweet);
    expect(terminationDetector.detect(basicTweet)).andStubReturn(null);
    expect(tweetDAO
        .findTweetsAfter(now.minusHours(TwitterServiceImpl.HOURS_BACK_TO_SEARCH), TRUCK_2_ID,
            false))
        .andStubReturn(tweets);
    matchStartTime = now.minusHours(3);
    matchEndTime = now.minusHours(2);
    matchedStop = new TruckStop(truck2, matchStartTime, matchEndTime, loca, null, false);
    uofc = Location.builder().lat(-3).lng(-43).name("58th and Ellis, Chicago, IL").build();
    expect(locator.locate("58th and Ellis, Chicago, IL", GeolocationGranularity.NARROW)).andStubReturn(uofc);
  }

  private void expectMatched(boolean softEnding) {
    final TruckStopMatch matched =
        TruckStopMatch.builder().stop(matchedStop).text(basicTweet.getText()).terminated(false)
            .softEnding(softEnding)
            .build();
    expect(matcher.match(truck2, basicTweet, null)).andStubReturn(matched);
  }

  private void expectTweetsIgnored() {
    // saves the lists of tweets
    tweetDAO.save(EasyMock.<List<TweetSummary>>anyObject());
  }

  @Test
  public void testKeepsOlderEventWhenNoOverlap() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop stopBeforeCurrent = new TruckStop(truck2,
        matchStartTime.minusHours(2), matchStartTime.minusHours(3), locb, null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(stopBeforeCurrent));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(matchedStop));
    expectTweetsIgnored();
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  private void expectOffTheRoad(boolean offTheRoad) {
    expect(offTheRoadDetector.offTheRoad(basicTweet.getText())).andReturn(offTheRoad);
  }

  @Test
  public void testStopEndsAfterMatchStart_sameLocationShouldMerge() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        new TruckStop(truck2, matchStartTime.minusMinutes(30),
            matchEndTime.minusMinutes(30), loca, null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(
        matchedStop.withStartTime(currentStop.getStartTime())));
    expectTweetsIgnored();
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testStopContainsMatch_sameLocation() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        new TruckStop(truck2, matchStartTime.minusMinutes(30),
            matchEndTime.plusMinutes(30), loca, null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(
        matchedStop.withStartTime(currentStop.getStartTime())));
    expectTweetsIgnored();
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testStopContainsMatch_sameLocationSoftEnding() {
    expectMatched(true);
    expectOffTheRoad(false);
    TruckStop currentStop =
        new TruckStop(truck2, matchStartTime.minusMinutes(30),
            matchEndTime.plusMinutes(30), loca, null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(
        matchedStop.withEndTime(currentStop.getEndTime())));
    expectTweetsIgnored();
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testMatchContainsCurrentStop() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        new TruckStop(truck2, matchStartTime.plusMinutes(3), matchEndTime.minusMinutes(3), loca,
            null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(
        ImmutableList.<TruckStop>of(matchedStop.withStartTime(currentStop.getStartTime())));
    expectTweetsIgnored();
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void whenOffTheRoadTriggerNotification() {
    expectOffTheRoad(true);
    emailNotifier.systemNotifyOffTheRoad(truck2, basicTweet);
    expect(matcher.match(truck2, basicTweet, null)).andStubReturn(null);
    expectTweetsIgnored();
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testStopStartsBeforeMatchEnds() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        new TruckStop(truck2, matchStartTime.plusMinutes(30), matchEndTime.plusHours(1), loca,
            null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay)).andReturn(
        ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.of(currentStop));
    truckStopDAO.addStops(
        ImmutableList.<TruckStop>of(matchedStop.withEndTime(currentStop.getEndTime())));
    expectTweetsIgnored();
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testKeepsFutureEventWhenNoOverlap() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop stopAfter = new TruckStop(truck2, matchEndTime.plusHours(1).toDateTime(),
        matchEndTime.plusHours(2).toDateTime(), loca, null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(stopAfter));
    expectTweetsIgnored();
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(matchedStop));
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testObserverTwittalyzerNoTweets() {
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false)).andReturn(ImmutableList.<TweetSummary>of());
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false)).andReturn(ImmutableList.<TweetSummary>of());
    replayAll();
    service.observerTwittalyze();
    verifyAll();
  }

  @Test
  public void testObserverTwittalyzerTweetsWithNoHashTag() {
    TweetSummary tweet1 = new TweetSummary.Builder().userId("uchinomgo").ignoreInTwittalyzer(false)
        .text("Today we have these food trucks: @CaponiesExp @threejsbbq").build();
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false))
        .andReturn(ImmutableList.<TweetSummary>of(tweet1));
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false)).andReturn(ImmutableList.<TweetSummary>of());
    tweetDAO.save(ImmutableList.of(tweet1));
    replayAll();
    service.observerTwittalyze();
    verifyAll();
  }

  @Test
  public void testObserverTwittalyzerTweetsNoExistingStops() {
    TweetSummary tweet1 = new TweetSummary.Builder().userId("uchinomgo").ignoreInTwittalyzer(false)
        .text("For lunch we have these #foodtrucks: @CaponiesExp @threejsbbq @somethingelse").build();
    truck1 = Truck.builder(truck1).twitterHandle("caponiesexp").id("caponiesexp").build();
    truck2 = Truck.builder(truck1).twitterHandle("threejsbbq").id("threejsbbq").build();
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false))
        .andReturn(ImmutableList.<TweetSummary>of(tweet1));
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false)).andReturn(ImmutableList.<TweetSummary>of());
    expect(truckDAO.findByTwitterId("caponiesexp")).andReturn(ImmutableList.of(truck1));
    expect(truckStopDAO.findDuring("caponiesexp", currentDay)).andReturn(ImmutableList.<TruckStop>of());
    expect(truckDAO.findByTwitterId("threejsbbq")).andReturn(ImmutableList.of(truck2));
    expect(truckStopDAO.findDuring("threejsbbq", currentDay)).andReturn(ImmutableList.<TruckStop>of(matchedStop));
    expect(truckDAO.findByTwitterId("somethingelse")).andReturn(ImmutableList.<Truck>of());
    truckStopDAO.addStops(ImmutableList.of(new TruckStop(truck1, now, now.plusHours(2), uofc, null, false)));
    emailNotifier.systemNotifyTrucksAddedByObserver(ImmutableMap.of(truck1, tweet1));
    tweetDAO.save(ImmutableList.of(tweet1));
    replayAll();
    service.observerTwittalyze();
    verifyAll();
  }

  @Test
  public void testMultiple() {
    TweetSummary tweet1 = new TweetSummary.Builder().userId("uchinomgo").ignoreInTwittalyzer(false)
        .text("For lunch we have these #foodtrucks: @CaponiesExp @threejsbbq @somethingelse").build();
    TweetSummary tweet2 = new TweetSummary.Builder().userId("mdw2mnl").ignoreInTwittalyzer(false)
        .text("For lunch we have these #foodtrucks: @CaponiesExp").build();
    truck1 = Truck.builder(truck1).twitterHandle("caponiesexp").id("caponiesexp").build();
    truck2 = Truck.builder(truck1).twitterHandle("threejsbbq").id("threejsbbq").build();
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false))
        .andReturn(ImmutableList.<TweetSummary>of(tweet1));
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false))
        .andReturn(ImmutableList.<TweetSummary>of(tweet2));
    expect(truckDAO.findByTwitterId("caponiesexp")).andReturn(ImmutableList.of(truck1)).times(2);
    expect(truckStopDAO.findDuring("caponiesexp", currentDay)).andReturn(ImmutableList.<TruckStop>of());
    expect(truckDAO.findByTwitterId("threejsbbq")).andReturn(ImmutableList.of(truck2));
    expect(truckStopDAO.findDuring("threejsbbq", currentDay)).andReturn(ImmutableList.<TruckStop>of(matchedStop));
    expect(truckDAO.findByTwitterId("somethingelse")).andReturn(ImmutableList.<Truck>of());
    truckStopDAO.addStops(ImmutableList.of(new TruckStop(truck1, now, now.plusHours(2), uofc, null, false)));
    emailNotifier.systemNotifyTrucksAddedByObserver(ImmutableMap.of(truck1, tweet1));
    tweetDAO.save(ImmutableList.of(tweet1));
    tweetDAO.save(ImmutableList.of(tweet2));
    replayAll();
    service.observerTwittalyze();
    verifyAll();
  }
}
