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

import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TweetCacheDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.model.TweetSummary;
import foodtruck.schedule.Confidence;
import foodtruck.schedule.TerminationDetector;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.TruckStopMatcher;
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

  @Before
  public void before() {
    final TwitterFactoryWrapper twitterFactory = createMock(TwitterFactoryWrapper.class);
    tweetDAO = createMock(TweetCacheDAO.class);
    final Truck truck1 = new Truck.Builder().id(TRUCK_1_ID).useTwittalyzer(false).build();
    truck2 = new Truck.Builder().id(TRUCK_2_ID).useTwittalyzer(true).build();
    final DateTimeZone zone = DateTimeZone.forID("America/Chicago");
    matcher = createMock(TruckStopMatcher.class);
    truckStopDAO = createMock(TruckStopDAO.class);
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
    expect(truckDAO.findAllTwitterTrucks()).andReturn(ImmutableSet.of( truck2));
    final int listId = 123;
    terminationDetector = createMock(TerminationDetector.class);
    service = new TwitterServiceImpl(twitterFactory, tweetDAO, listId, zone, matcher,
        truckStopDAO,
        clock, terminationDetector, new LocalCacheUpdater(), truckDAO);
    loca = Location.builder().lat(1).lng(2).name("a").build();
    locb = Location.builder().lat(3).lng(4).name("b").build();
    final TweetSummary basicTweet = new TweetSummary.Builder().time(now.minusHours(2)).text(
        "We are at Kingsbury and Erie.").build();
    List<TweetSummary> tweets = ImmutableList.of(basicTweet);
    expect(terminationDetector.detect(basicTweet)).andStubReturn(null);
    expect(tweetDAO
        .findTweetsAfter(now.minusHours(TwitterServiceImpl.HOURS_BACK_TO_SEARCH), TRUCK_2_ID, false))
        .andStubReturn(tweets);
    matchStartTime = now.minusHours(3);
    matchEndTime = now.minusHours(2);
    matchedStop = new TruckStop(truck2, matchStartTime, matchEndTime, loca, null, false);
    final TruckStopMatch matched =
        new TruckStopMatch(Confidence.HIGH, matchedStop, basicTweet.getText(), false);
    expect(matcher.match(truck2, basicTweet, null)).andStubReturn(matched);
  }

  private void expectTweetsIgnored(List<TweetSummary> tweets) {
    // saves the lists of tweets
    tweetDAO.save(EasyMock.<List<TweetSummary>>anyObject());
  }

  @Test
  public void testKeepsOlderEventWhenNoOverlap() {
    TruckStop stopBeforeCurrent = new TruckStop(truck2,
        matchStartTime.minusHours(2), matchStartTime.minusHours(3), locb, null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(stopBeforeCurrent));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(matchedStop));
    expectTweetsIgnored(null);
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testStopEndsAfterMatchStart_sameLocationShouldMerge() {
    TruckStop currentStop =
        new TruckStop(truck2, matchStartTime.minusMinutes(30),
            matchEndTime.minusMinutes(30), loca, null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(
        matchedStop.withStartTime(currentStop.getStartTime())));
    expectTweetsIgnored(null);
    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testMatchContainsCurrentStop() {
    TruckStop currentStop =
        new TruckStop(truck2, matchStartTime.plusMinutes(3), matchEndTime.minusMinutes(3), loca,
            null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(
        ImmutableList.<TruckStop>of(matchedStop.withStartTime(currentStop.getStartTime())));
    expectTweetsIgnored(null);
    replayAll();
    service.twittalyze();
    verifyAll();
  }


  @Test
  public void testStopStartsBeforeMatchEnds() {
    TruckStop currentStop =
        new TruckStop(truck2, matchStartTime.plusMinutes(30), matchEndTime.plusHours(1), loca,
            null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay)).andReturn(
        ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.of(currentStop));
    truckStopDAO.addStops(
        ImmutableList.<TruckStop>of(matchedStop.withEndTime(currentStop.getEndTime())));
    expectTweetsIgnored(null);

    replayAll();
    service.twittalyze();
    verifyAll();
  }

  @Test
  public void testKeepsFutureEventWhenNoOverlap() {
    TruckStop stopAfter = new TruckStop(truck2, matchEndTime.plusHours(1).toDateTime(),
        matchEndTime.plusHours(2).toDateTime(), loca, null, false);
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(stopAfter));
    expectTweetsIgnored(null);
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(matchedStop));
    replayAll();
    service.twittalyze();
    verifyAll();
  }
}
