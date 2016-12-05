package foodtruck.schedule;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.RetweetsDAO;
import foodtruck.dao.StoryDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckObserverDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckObserver;
import foodtruck.model.TruckStop;
import foodtruck.model.TwitterNotificationAccount;
import foodtruck.notifications.PublicEventNotificationService;
import foodtruck.mail.SystemNotificationService;
import foodtruck.socialmedia.SocialMediaConnector;
import foodtruck.time.Clock;

import static org.easymock.EasyMock.expect;

/**
 * @author aviolette@gmail.com
 * @since 10/18/11
 */
public class SocialMediaCacherImplTest extends EasyMockSupport {

  private static final String TRUCK_1_ID = "truck1";
  private static final String TRUCK_2_ID = "truck2";
  private StoryDAO tweetDAO;
  private TruckStopMatcher matcher;
  private TruckStopDAO truckStopDAO;
  private SocialMediaCacherImpl service;
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
  private Story basicTweet;
  private SystemNotificationService emailNotifier;
  private OffTheRoadDetector offTheRoadDetector;
  private GeoLocator locator;
  private Location uofc;
  private Truck truck1;
  private TruckObserverDAO truckObserverDAO;
  private RetweetsDAO retweetDAO;
  private TwitterNotificationAccountDAO notificationDAO;
  private SpecialUpdater specialsUpdater;
  private StoryEventCallback notificationService;

  @Before
  public void before() {
    tweetDAO = createMock(StoryDAO.class);
    retweetDAO = createNiceMock(RetweetsDAO.class);
    truck1 = new Truck.Builder().id(TRUCK_1_ID).twitterHandle(TRUCK_1_ID)
        .useTwittalyzer(false).build();
    truck2 = new Truck.Builder().id(TRUCK_2_ID).twitterHandle(TRUCK_2_ID)
        .useTwittalyzer(true).build();
    final DateTimeZone zone = DateTimeZone.forID("America/Chicago");
    emailNotifier = createMock(SystemNotificationService.class);
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
    expect(truckDAO.findAll()).andStubReturn(ImmutableList.of(truck2));
    terminationDetector = createMock(TerminationDetector.class);
    specialsUpdater = createMock(SpecialUpdater.class);
    specialsUpdater.update(EasyMock.anyObject(Truck.class), EasyMock.anyObject(List.class));
    EasyMock.expectLastCall().anyTimes();
    offTheRoadDetector = createMock(OffTheRoadDetector.class);
    truckObserverDAO = createMock(TruckObserverDAO.class);
    notificationDAO = createMock(TwitterNotificationAccountDAO.class);
    expect(notificationDAO.findAll()).andStubReturn(ImmutableList.<TwitterNotificationAccount>of());
    DateTimeFormatter timeFormatter = DateTimeFormat.longTime();
    expect(clock.nowFormattedAsTime()).andStubReturn(timeFormatter.print(now));
    Set<SocialMediaConnector> connectors = ImmutableSet.of();
    notificationService = createNiceMock(StoryEventCallback.class);
    service = new SocialMediaCacherImpl( tweetDAO, matcher,
        truckStopDAO, clock, terminationDetector, truckDAO, emailNotifier, offTheRoadDetector, locator,
        truckObserverDAO, null, timeFormatter, new StaticConfig(), connectors, specialsUpdater, notificationService);
    loca = Location.builder().lat(1).lng(2).name("a").build();
    locb = Location.builder().lat(3).lng(4).name("b").build();
    basicTweet = new Story.Builder().time(now.minusHours(2)).text(
        "We are at Kingsbury and Erie.").build();
    List<Story> tweets = ImmutableList.of(basicTweet);
    expect(terminationDetector.detect(basicTweet)).andStubReturn(null);
    expect(tweetDAO
        .findTweetsAfter(now.minusHours(SocialMediaCacherImpl.HOURS_BACK_TO_SEARCH), TRUCK_2_ID,
            false))
        .andStubReturn(tweets);
    matchStartTime = now.minusHours(3);
    matchEndTime = now.minusHours(2);
    matchedStop = TruckStop.builder().truck(truck2).startTime(matchStartTime).endTime(matchEndTime).location(loca).build();
    uofc = Location.builder().lat(-3).lng(-43).name("58th and Ellis, Chicago, IL").build();
    expect(locator.locate("58th and Ellis, Chicago, IL", GeolocationGranularity.NARROW)).andStubReturn(uofc);
  }

  private void expectMatched(boolean softEnding) {
    final TruckStopMatch matched = TruckStopMatch.builder()
        .stop(matchedStop)
        .story(Story.builder().text(basicTweet.getText()).build())
            .softEnding(softEnding)
            .build();
    expect(matcher.match(truck2, basicTweet)).andStubReturn(matched);
  }

  private void expectTweetsIgnored() {
    // saves the lists of tweets
    tweetDAO.save(EasyMock.<List<Story>>anyObject());
  }

  @Test
  public void testKeepsOlderEventWhenNoOverlap() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop stopBeforeCurrent = TruckStop.builder().truck(truck2).startTime(matchStartTime.minusHours(2))
        .endTime(matchStartTime.minusHours(3)).location(locb).build();
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(stopBeforeCurrent)).times(2);
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(matchedStop));
    expectTweetsIgnored();
    replayAll();
    service.handleTruckStories();
    verifyAll();
  }

  private void expectOffTheRoad(boolean offTheRoad) {
    expect(offTheRoadDetector.offTheRoad(basicTweet.getText())).andReturn(new OffTheRoadResponse(offTheRoad, false));
  }

  @Test
  public void testStopEndsAfterMatchStart_sameLocationShouldMerge() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        TruckStop.builder().truck(truck2).startTime(matchStartTime.minusMinutes(30))
            .endTime(matchEndTime.minusMinutes(30)).location(loca).build();
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop)).times(2);
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(
        matchedStop.withStartTime(currentStop.getStartTime())));
    expectTweetsIgnored();
    replayAll();
    service.handleTruckStories();
    verifyAll();
  }

  @Test
  public void testStopContainsMatch_sameLocation() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        TruckStop.builder().truck(truck2).startTime(matchStartTime.minusMinutes(30)).endTime(matchEndTime.plusMinutes(30)).location(loca).build();
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop)).times(2);
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(
        matchedStop.withStartTime(currentStop.getStartTime())));
    expectTweetsIgnored();
    replayAll();
    service.handleTruckStories();
    verifyAll();
  }

  @Test
  public void testStopContainsMatch_sameLocationSoftEnding() {
    expectMatched(true);
    expectOffTheRoad(false);
    TruckStop currentStop =
        TruckStop.builder().truck(truck2).startTime(matchStartTime.minusMinutes(30))
            .endTime(matchEndTime.plusMinutes(30)).location(loca).build();
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop)).times(2);
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(
        matchedStop.withStartTime(currentStop.getStartTime())
            .withEndTime(currentStop.getEndTime())));
    expectTweetsIgnored();
    replayAll();
    service.handleTruckStories();
    verifyAll();
  }

  @Test
  public void testMatchContainsCurrentStop() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        TruckStop.builder().truck(truck2).startTime( matchStartTime.plusMinutes(3))
            .endTime(matchEndTime.minusMinutes(3)).location(loca).build();
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(currentStop)).times(2);
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(
        ImmutableList.<TruckStop>of(matchedStop.withStartTime(currentStop.getStartTime())));
    expectTweetsIgnored();
    replayAll();
    service.handleTruckStories();
    verifyAll();
  }

  @Test
  public void whenOffTheRoadTriggerNotification() {
    expectOffTheRoad(true);
    emailNotifier.systemNotifyOffTheRoad(truck2, basicTweet);
    expect(matcher.match(truck2, basicTweet)).andStubReturn(null);
    expectTweetsIgnored();
    replayAll();
    service.handleTruckStories();
    verifyAll();
  }

  @Test
  public void testStopStartsBeforeMatchEnds() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        TruckStop.builder().truck(truck2).startTime(matchStartTime.plusMinutes(30))
            .endTime(matchEndTime.plusHours(1)).location(loca).build();
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay)).andReturn(
        ImmutableList.<TruckStop>of(currentStop)).times(2);
    truckStopDAO.deleteStops(ImmutableList.of(currentStop));
    truckStopDAO.addStops(
        ImmutableList.<TruckStop>of(matchedStop.withEndTime(currentStop.getEndTime())));
    expectTweetsIgnored();
    replayAll();
    service.handleTruckStories();
    verifyAll();
  }

  @Test
  public void testKeepsFutureEventWhenNoOverlap() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop stopAfter =
        TruckStop.builder().truck(truck2).startTime(matchEndTime.plusHours(1).toDateTime()).endTime(matchEndTime.plusHours(2).toDateTime()).location(loca).build();
    expect(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .andReturn(ImmutableList.<TruckStop>of(stopAfter)).times(2);
    expectTweetsIgnored();
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(matchedStop));
    replayAll();
    service.handleTruckStories();
    verifyAll();
  }

  @Test
  public void testObserverTwittalyzerNoTweets() {
    Location uchicago = Location.builder().lat(-234).lng(-432).name("University of Chicago").build();
    expect(truckObserverDAO.findAll()).andReturn(ImmutableList.of(
        new TruckObserver("uchinomgo", uchicago, ImmutableList.of("breakfast", "lunch")),
        new TruckObserver("mdw2mnl", uchicago, ImmutableList.of("breakfast", "lunch"))));
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false)).andReturn(ImmutableList.<Story>of());
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false)).andReturn(ImmutableList.<Story>of());
    replayAll();
    service.observerAnalyze();
    verifyAll();
  }

  @Test
  public void testObserverTwittalyzerTweetsWithNoHashTag() {
    Location uchicago = Location.builder().lat(-234).lng(-432).name("University of Chicago").build();
    expect(truckObserverDAO.findAll()).andReturn(ImmutableList.of(
        new TruckObserver("uchinomgo", uchicago, ImmutableList.of("breakfast", "lunch")),
        new TruckObserver("mdw2mnl", uchicago, ImmutableList.of("breakfast", "lunch"))));
    Story tweet1 = new Story.Builder().userId("uchinomgo").ignoreInTwittalyzer(false)
        .text("Today we have these food trucks: @CaponiesExp @threejsbbq").build();
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false))
        .andReturn(ImmutableList.<Story>of(tweet1));
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false)).andReturn(ImmutableList.<Story>of());
    tweetDAO.save(ImmutableList.of(tweet1));
    replayAll();
    service.observerAnalyze();
    verifyAll();
  }

  @Test
  public void testObserverTwittalyzerTweetsNoExistingStops() {
    Location uchicago = Location.builder()
        .lat(-40)
        .lng(-80)
        .name("University of Chicago")
        .build();
    expect(truckObserverDAO.findAll()).andReturn(ImmutableList.of(
        new TruckObserver("uchinomgo", uchicago, ImmutableList.of("breakfast", "lunch")),
        new TruckObserver("mdw2mnl", uchicago, ImmutableList.of("breakfast", "lunch"))));
    Story tweet1 = new Story.Builder().userId("uchinomgo").ignoreInTwittalyzer(false)
        .text("breakfast: @CaponiesExp @threejsbbq @somethingelse").build();
    truck1 = Truck.builder(truck1).categories(ImmutableSet.of("Breakfast")).twitterHandle("caponiesexp").id("caponiesexp").build();
    truck2 = Truck.builder(truck1).categories(ImmutableSet.of("Breakfast")).twitterHandle("threejsbbq").id("threejsbbq").build();
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false))
        .andReturn(ImmutableList.<Story>of(tweet1));
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false)).andReturn(ImmutableList.<Story>of());
    expect(truckDAO.findByTwitterId("caponiesexp")).andReturn(ImmutableList.of(truck1));
    expect(truckStopDAO.findDuring("caponiesexp", currentDay)).andReturn(ImmutableList.<TruckStop>of());
    expect(truckDAO.findByTwitterId("threejsbbq")).andReturn(ImmutableList.of(truck2));
    expect(truckStopDAO.findDuring("threejsbbq", currentDay)).andReturn(ImmutableList.<TruckStop>of(matchedStop));
    expect(truckDAO.findByTwitterId("somethingelse")).andReturn(ImmutableList.<Truck>of());
    truckStopDAO.addStops(ImmutableList.of(TruckStop.builder()
        .truck(truck1)
        .startTime(now)
        .endTime(now.plusHours(2))
        .location(uchicago)
        .build()));
    emailNotifier.systemNotifyTrucksAddedByObserver(ImmutableMap.of(truck1, tweet1));
    tweetDAO.save(ImmutableList.of(tweet1));
    replayAll();
    service.observerAnalyze();
    verifyAll();
  }

  @Test
  public void testMultiple() {
    Location uchicago = Location.builder()
        .lat(-40)
        .lng(-80)
        .name("University of Chicago")
        .build();
    expect(truckObserverDAO.findAll()).andReturn(ImmutableList.of(
        new TruckObserver("uchinomgo", uchicago, ImmutableList.of("breakfast", "#foodtrucks")),
        new TruckObserver("mdw2mnl", uchicago, ImmutableList.of("breakfast", "lunch"))));
    Story tweet1 = new Story.Builder().userId("uchinomgo").ignoreInTwittalyzer(false)
        .text("we have these #foodtrucks: @CaponiesExp @threejsbbq @somethingelse").build();
    Story tweet2 = new Story.Builder().userId("mdw2mnl").ignoreInTwittalyzer(false)
        .text("we have these #foodtrucks: @CaponiesExp").build();
    truck1 = Truck.builder(truck1).categories(ImmutableSet.of("Breakfast")).twitterHandle("caponiesexp").id("caponiesexp").build();
    truck2 = Truck.builder(truck1).categories(ImmutableSet.of("Breakfast")).twitterHandle("threejsbbq").id("threejsbbq").build();
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false))
        .andReturn(ImmutableList.<Story>of(tweet1));
    expect(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false))
        .andReturn(ImmutableList.<Story>of(tweet2));
    expect(truckDAO.findByTwitterId("caponiesexp")).andReturn(ImmutableList.of(truck1)).times(1);
    expect(truckStopDAO.findDuring("caponiesexp", currentDay)).andReturn(ImmutableList.<TruckStop>of());
    expect(truckDAO.findByTwitterId("threejsbbq")).andReturn(ImmutableList.of(truck2));
    expect(truckStopDAO.findDuring("threejsbbq", currentDay)).andReturn(ImmutableList.<TruckStop>of(matchedStop));
    expect(truckDAO.findByTwitterId("somethingelse")).andReturn(ImmutableList.<Truck>of());
    truckStopDAO.addStops(ImmutableList.of(TruckStop.builder()
        .truck(truck1)
        .startTime(now)
        .endTime(now.plusHours(2))
        .location(uchicago)
        .build()));
    emailNotifier.systemNotifyTrucksAddedByObserver(ImmutableMap.of(truck1, tweet1));
    tweetDAO.save(ImmutableList.of(tweet1));
    tweetDAO.save(ImmutableList.of(tweet2));
    replayAll();
    service.observerAnalyze();
    verifyAll();
  }

  @Test
  public void testHandleNotificationsForMentionedTrucksNoneMentioned() {
    final TruckStopMatch matched = TruckStopMatch.builder()
        .stop(matchedStop)
        .story(Story.builder().text(basicTweet.getText()).build())
            .build();
    replayAll();
    service.handleAdditionalTrucks(matchedStop, matched);
    verifyAll();
  }

  @Test
  public void testHandleNotificationsForMentionedTrucksMentionsNonTruck() {
    basicTweet = Story.builder(basicTweet).text("We are here at @foobar on Clark and Monroe").build();
    final TruckStopMatch matched = TruckStopMatch.builder()
        .stop(matchedStop)
        .story(Story.builder().text(basicTweet.getText()).build())
            .build();
    expect(truckDAO.findByTwitterId("foobar")).andReturn(ImmutableSet.<Truck>of());
    replayAll();
    service.handleAdditionalTrucks(matchedStop, matched);
    verifyAll();
  }

  @Test
  public void testHandleNotificationsForMentionedTrucksMentionsTruck() {
    basicTweet = Story.builder(basicTweet).text("We are here at @truck1 on Clark and Monroe").build();
    final TruckStopMatch matched = TruckStopMatch.builder()
        .stop(matchedStop)
        .story(Story.builder().text(basicTweet.getText()).build())
            .build();
    expect(truckDAO.findByTwitterId("truck1")).andReturn(ImmutableSet.of(truck1));
    expect(truckStopDAO.findOverRange("truck1", matchedStop.getInterval()))
        .andReturn(ImmutableList.<TruckStop>of());
    emailNotifier.notifyAddMentionedTrucks(ImmutableSet.of("truck1"), matchedStop, basicTweet.getText());
    replayAll();
    service.handleAdditionalTrucks(matchedStop, matched);
    verifyAll();
  }
}
