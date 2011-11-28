package foodtruck.twitter;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TweetCacheDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.model.Trucks;
import foodtruck.model.TweetSummary;
import foodtruck.schedule.Confidence;
import foodtruck.schedule.TerminationDetector;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.TruckStopMatcher;
import foodtruck.util.Clock;
import static org.easymock.EasyMock.and;
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
  private static final String TRUCK_3_ID = "truck3";
  private Truck truck2;
  private Truck truck3;
  private DateTime now;
  private LocalDate currentDay;
  private TerminationDetector terminationDetector;
  private Location loca;
  private Location locb;
  private Location locc;
  private TweetSummary basicTweet;
  private TruckStop matchedStop;
  private TruckStopMatch matched;
  private DateTime matchStartTime;
  private DateTime matchEndTime;

  @Before
  public void before() {
    final TwitterFactoryWrapper twitterFactory = createMock(TwitterFactoryWrapper.class);
    tweetDAO = createMock(TweetCacheDAO.class);
    final Truck truck1 = new Truck.Builder().id(TRUCK_1_ID).useTwittalyzer(false).build();
    truck2 = new Truck.Builder().id(TRUCK_2_ID).useTwittalyzer(true).build();
    truck3 = new Truck.Builder().id(TRUCK_3_ID).useTwittalyzer(false).build();
    final DateTimeZone zone = DateTimeZone.forID("America/Chicago");
    matcher = createMock(TruckStopMatcher.class);
    truckStopDAO = createMock(TruckStopDAO.class);
    final Clock clock = createMock(Clock.class);
    now = new DateTime(2011, 10, 9, 8, 0, 0, 0, zone);
    currentDay = now.toLocalDate();
    expect(clock.now()).andStubReturn(now);
    expect(clock.currentDay()).andStubReturn(currentDay);
    ImmutableMap<String, Truck> truckMap =
        ImmutableMap.of(TRUCK_1_ID, truck1, TRUCK_2_ID, truck2, TRUCK_3_ID, truck3);
    final Trucks trucks = new Trucks(truckMap);
    final int listId = 123;
    terminationDetector = createMock(TerminationDetector.class);
    service = new TwitterServiceImpl(twitterFactory, tweetDAO, listId, trucks, zone, matcher,
        truckStopDAO,
        clock, terminationDetector, new LocalCacheUpdater());
    loca = new Location(1, 2, "a");
    locb = new Location(3, 4, "b");
    locc = new Location(4, 5, "c");
    basicTweet = new TweetSummary.Builder().time(now.minusHours(2)).text(
        "We are at Kingsbury and Erie.").build();
    List<TweetSummary> tweets = ImmutableList.of(basicTweet);
    expect(terminationDetector.detect(basicTweet)).andStubReturn(null);
    expect(tweetDAO
        .findTweetsAfter(now.minusHours(TwitterServiceImpl.HOURS_BACK_TO_SEARCH), TRUCK_2_ID))
        .andStubReturn(tweets);
    matchStartTime = now.minusHours(3);
    matchEndTime = now.minusHours(2);
    matchedStop = new TruckStop(truck2, matchStartTime, matchEndTime, loca, null);
    matched = new TruckStopMatch(Confidence.HIGH, matchedStop, basicTweet.getText(), false);
    expect(matcher.match(truck2, basicTweet, null)).andStubReturn(matched);
  }

  // Terminates last matching tweet at current time if a 'stop phrase' is found.
  @Test
  public void testFindsTerminationTweetAndCapsLastTweet() {
    TweetSummary tweet1 = new TweetSummary.Builder()
        .text("sold out of #tamales for the day.. THANKS #chicago.. See you tomorrow!!").build();
    TweetSummary tweet2 = new TweetSummary.Builder().text("tweet2").build();
    expect(terminationDetector.detect(tweet1)).andReturn(now);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of());
    List<TweetSummary> tweets = ImmutableList.of(tweet1, tweet2);
    TruckStop stop =
        new TruckStop(truck2, now.minusHours(3), now.minusHours(2), new Location(-1, -2, "Foobar"),
            null);
    TruckStopMatch match = new TruckStopMatch(Confidence.HIGH, stop, "tweet2", false);
    expect(tweetDAO
        .findTweetsAfter(now.minusHours(TwitterServiceImpl.HOURS_BACK_TO_SEARCH), TRUCK_2_ID))
        .andReturn(tweets);
    expect(matcher.match(truck2, tweet2, now)).andReturn(match);
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(stop));
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testKeepsOlderEventWhenNoOverlap() {
    TruckStop stopBeforeCurrent =
        new TruckStop(truck2, now.minusHours(5).toDateTime(),
            now.minusHours(TwitterServiceImpl.HOURS_BACK_TO_SEARCH), locb, null);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(stopBeforeCurrent));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(matchedStop));
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testStopEndsAfterMatchStart_sameLocationShouldMerge() {
    TruckStop currentStop =
        new TruckStop(truck2, matchStartTime.minusMinutes(30),
            matchEndTime.minusMinutes(30), loca, null);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(
        matchedStop.withStartTime(currentStop.getStartTime())));
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testMatchContainsCurrentStop() {
    TruckStop currentStop =
        new TruckStop(truck2, matchStartTime.plusMinutes(3), matchEndTime.minusMinutes(3), loca, null);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(
        ImmutableList.<TruckStop>of(matchedStop.withStartTime(currentStop.getStartTime())));
    replayAll();
    service.twittalyze();
    verifyAll();
  }


  @Test
  public void testStopStartsBeforeMatchEnds() {
   TruckStop currentStop =
        new TruckStop(truck2, matchStartTime.plusMinutes(30), matchEndTime.plusHours(1), loca, null);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay)).andReturn(
        ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.of(currentStop));
    truckStopDAO.addStops(
        ImmutableList.<TruckStop>of(matchedStop.withEndTime(currentStop.getEndTime())));
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testKeepsFutureEventWhenNoOverlap() {
    TweetSummary tweet1 = new TweetSummary.Builder().time(now.minusHours(2)).text(
        "We are at Kingsbury and Erie.").build();
    expect(terminationDetector.detect(tweet1)).andReturn(null);
    List<TweetSummary> tweets = ImmutableList.of(tweet1);
    expect(tweetDAO
        .findTweetsAfter(now.minusHours(TwitterServiceImpl.HOURS_BACK_TO_SEARCH), TRUCK_2_ID))
        .andReturn(tweets);
    TruckStop stop =
        new TruckStop(truck2, now.minusHours(3), now.minusHours(2), new Location(-1, -2, "Foobar"),
            null);
    TruckStopMatch match = new TruckStopMatch(Confidence.HIGH, stop, tweet1.getText(), false);
    expect(matcher.match(truck2, tweet1, null)).andReturn(match);
    Location loc = new Location(-3, -4, "First Location");
    TruckStop stopAfter = new TruckStop(truck2, now.minusHours(1).toDateTime(), now, loc, null);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(stopAfter));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(stop));
    replayAll();
    service.twittalyze();
    verifyAll();
  }
}
