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
  private static final String TRUCK_3_ID = "truck3";
  private Truck truck2;
  private Truck truck3;
  private DateTime now;

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
    final LocalDate currentDay = now.toLocalDate();
    expect(clock.now()).andStubReturn(now);
    expect(clock.currentDay()).andStubReturn(currentDay);
    ImmutableMap<String, Truck> truckMap =
        ImmutableMap.of(TRUCK_1_ID, truck1, TRUCK_2_ID, truck2, TRUCK_3_ID, truck3);
    final Trucks trucks = new Trucks(truckMap);
    final int listId = 123;
    service = new TwitterServiceImpl(twitterFactory, tweetDAO, listId, trucks, zone, matcher, truckStopDAO,
        clock);
  }

  // This will not be the default behavior, but is now
  @Test
  public void testAddsAStopAndDeletesAllElseWhenMatchIsMade() {
    TweetSummary tweet1 = new TweetSummary.Builder().text("tweet1").build();
    TweetSummary tweet2 = new TweetSummary.Builder().text("tweet2").build();
    List<TweetSummary> tweets = ImmutableList.of(tweet1, tweet2);
    TruckStop stop = new TruckStop(truck2, now.minusHours(3), now.minusHours(2), new Location(-1, -2, "Foobar"), null);
    TruckStopMatch match = new TruckStopMatch(Confidence.HIGH, stop, "tweet2");
    expect(tweetDAO.findTweetsAfter(now.minusHours(4), TRUCK_2_ID)).andReturn(tweets);
    expect(matcher.match(truck2, tweet1, null)).andReturn(null);
    expect(matcher.match(truck2, tweet2, null)).andReturn(match);
    truckStopDAO.deleteAfter(now.toDateMidnight().toDateTime(), TRUCK_2_ID);
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(stop));
    replayAll();
    service.updateLocationsOfTwitterTrucks();
    verifyAll();
  }

  // Terminates last matching tweet at current time if a 'stop phrase' is found.
  @Test
  public void testFindsTerminationTweetAndCapsLastTweet() {
    TweetSummary tweet1 = new TweetSummary.Builder().text("sold out of #tamales for the day.. THANKS #chicago.. See you tomorrow!!").build();
    TweetSummary tweet2 = new TweetSummary.Builder().text("tweet2").build();
    List<TweetSummary> tweets = ImmutableList.of(tweet1, tweet2);
    TruckStop stop = new TruckStop(truck2, now.minusHours(3), now.minusHours(2), new Location(-1, -2, "Foobar"), null);
    TruckStopMatch match = new TruckStopMatch(Confidence.HIGH, stop, "tweet2");
    expect(tweetDAO.findTweetsAfter(now.minusHours(4), TRUCK_2_ID)).andReturn(tweets);
    expect(matcher.match(truck2, tweet2, now)).andReturn(match);
    truckStopDAO.deleteAfter(now.toDateMidnight().toDateTime(), TRUCK_2_ID);
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(stop));
    replayAll();
    service.updateLocationsOfTwitterTrucks();
    verifyAll();
  }
}
