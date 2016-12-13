package foodtruck.schedule;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.RetweetsDAO;
import foodtruck.dao.StoryDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckObserverDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.mail.SystemNotificationService;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckObserver;
import foodtruck.model.TruckStop;
import foodtruck.socialmedia.SocialMediaConnector;
import foodtruck.time.Clock;

/**
 * @author aviolette@gmail.com
 * @since 10/18/11
 */
@RunWith(MockitoJUnitRunner.class)
public class SocialMediaCacherImplTest extends Mockito {

  private static final String TRUCK_1_ID = "truck1";
  private static final String TRUCK_2_ID = "truck2";
  @Mock private RetweetsDAO retweetDAO;
  @Mock private StoryDAO tweetDAO;
  @Mock private SystemNotificationService emailNotifier;
  @Mock private TruckStopMatcher matcher;
  @Mock private TruckStopDAO truckStopDAO;
  @Mock private GeoLocator locator;
  @Mock private TerminationDetector terminationDetector;
  @Mock private TruckDAO truckDAO;
  @Mock private SpecialUpdater specialsUpdater;
  @Mock private TruckObserverDAO truckObserverDAO;
  @Mock private OffTheRoadDetector offTheRoadDetector;
  private SocialMediaCacherImpl service;
  private Truck truck2;
  private DateTime now;
  private LocalDate currentDay;
  private Location loca;
  private Location locb;
  private TruckStop matchedStop;
  private DateTime matchStartTime;
  private DateTime matchEndTime;
  private Story basicTweet;
  private Location uofc;
  private Truck truck1;
  private TwitterNotificationAccountDAO notificationDAO;
  private StoryEventCallback notificationService;

  @Before
  public void before() {
    truck1 = new Truck.Builder().id(TRUCK_1_ID).twitterHandle(TRUCK_1_ID)
        .useTwittalyzer(false).build();
    truck2 = new Truck.Builder().id(TRUCK_2_ID).twitterHandle(TRUCK_2_ID)
        .useTwittalyzer(true).build();
    final DateTimeZone zone = DateTimeZone.forID("America/Chicago");
    final Clock clock = mock(Clock.class);
    now = new DateTime(2011, 10, 9, 8, 0, 0, 0, zone);
    currentDay = now.toLocalDate();
    when(clock.now()).thenReturn(now);
    when(clock.currentDay()).thenReturn(currentDay);
    ImmutableMap<String, Truck> truckMap =
        ImmutableMap.of(TRUCK_1_ID, truck1, TRUCK_2_ID, truck2);
    when(truckDAO.findById(TRUCK_1_ID)).thenReturn(truck1);
    when(truckDAO.findById(TRUCK_2_ID)).thenReturn(truck2);
    when(truckDAO.findAll()).thenReturn(ImmutableList.of(truck2));
    DateTimeFormatter timeFormatter = DateTimeFormat.longTime();
    Set<SocialMediaConnector> connectors = ImmutableSet.of();
    service = new SocialMediaCacherImpl( tweetDAO, matcher,
        truckStopDAO, clock, terminationDetector, truckDAO, emailNotifier, offTheRoadDetector, locator,
        truckObserverDAO, null, timeFormatter, new StaticConfig(), connectors, specialsUpdater, notificationService);
    loca = Location.builder().lat(1).lng(2).name("a").build();
    locb = Location.builder().lat(3).lng(4).name("b").build();
    basicTweet = new Story.Builder().time(now.minusHours(2)).text(
        "We are at Kingsbury and Erie.").build();
    List<Story> tweets = ImmutableList.of(basicTweet);
    matchStartTime = now.minusHours(3);
    matchEndTime = now.minusHours(2);
    matchedStop = TruckStop.builder().truck(truck2).startTime(matchStartTime).endTime(matchEndTime).location(loca).build();
    uofc = Location.builder().lat(-3).lng(-43).name("58th and Ellis, Chicago, IL").build();
  }

  private void expectMatched(boolean softEnding) {
    final TruckStopMatch matched = TruckStopMatch.builder()
        .stop(matchedStop)
        .story(Story.builder().text(basicTweet.getText()).build())
            .softEnding(softEnding)
            .build();
    when(matcher.match(truck2, basicTweet)).thenReturn(matched);
  }

  private void expectTweetsIgnored() {
    // saves the lists of tweets
    tweetDAO.save((List<Story>) anyObject());
  }

  @Test
  public void testKeepsOlderEventWhenNoOverlap() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop stopBeforeCurrent = TruckStop.builder().truck(truck2).startTime(matchStartTime.minusHours(2))
        .endTime(matchStartTime.minusHours(3)).location(locb).build();
    when(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .thenReturn(ImmutableList.<TruckStop>of(stopBeforeCurrent));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(matchedStop));
    expectTweetsIgnored();
    
    service.handleTruckStories();
    
  }

  private void expectOffTheRoad(boolean offTheRoad) {
    when(offTheRoadDetector.offTheRoad(basicTweet.getText())).thenReturn(new OffTheRoadResponse(offTheRoad, false));
  }

  @Test
  public void testStopEndsAfterMatchStart_sameLocationShouldMerge() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        TruckStop.builder().truck(truck2).startTime(matchStartTime.minusMinutes(30))
            .endTime(matchEndTime.minusMinutes(30)).location(loca).build();
    when(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .thenReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(
        matchedStop.withStartTime(currentStop.getStartTime())));
    expectTweetsIgnored();
    
    service.handleTruckStories();
    
  }

  @Test
  public void testStopContainsMatch_sameLocation() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        TruckStop.builder().truck(truck2).startTime(matchStartTime.minusMinutes(30)).endTime(matchEndTime.plusMinutes(30)).location(loca).build();
    when(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .thenReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(
        matchedStop.withStartTime(currentStop.getStartTime())));
    expectTweetsIgnored();
    
    service.handleTruckStories();
    
  }

  @Test
  public void testStopContainsMatch_sameLocationSoftEnding() {
    expectMatched(true);
    expectOffTheRoad(false);
    TruckStop currentStop =
        TruckStop.builder().truck(truck2).startTime(matchStartTime.minusMinutes(30))
            .endTime(matchEndTime.plusMinutes(30)).location(loca).build();
    when(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .thenReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(
        matchedStop.withStartTime(currentStop.getStartTime())
            .withEndTime(currentStop.getEndTime())));
    expectTweetsIgnored();
    
    service.handleTruckStories();
    
  }

  @Test
  public void testMatchContainsCurrentStop() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        TruckStop.builder().truck(truck2).startTime( matchStartTime.plusMinutes(3))
            .endTime(matchEndTime.minusMinutes(3)).location(loca).build();
    when(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .thenReturn(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.addStops(
        ImmutableList.<TruckStop>of(matchedStop.withStartTime(currentStop.getStartTime())));
    expectTweetsIgnored();
    
    service.handleTruckStories();
    
  }

  @Test
  public void whenOffTheRoadTriggerNotification() {
    expectOffTheRoad(true);
    emailNotifier.systemNotifyOffTheRoad(truck2, basicTweet);
    when(matcher.match(truck2, basicTweet)).thenReturn(null);
    expectTweetsIgnored();
    
    service.handleTruckStories();
    
  }

  @Test
  public void testStopStartsBeforeMatchEnds() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop currentStop =
        TruckStop.builder().truck(truck2).startTime(matchStartTime.plusMinutes(30))
            .endTime(matchEndTime.plusHours(1)).location(loca).build();
    when(truckStopDAO.findDuring(TRUCK_2_ID, currentDay)).thenReturn(
        ImmutableList.<TruckStop>of(currentStop));
    truckStopDAO.deleteStops(ImmutableList.of(currentStop));
    truckStopDAO.addStops(
        ImmutableList.<TruckStop>of(matchedStop.withEndTime(currentStop.getEndTime())));
    expectTweetsIgnored();
    
    service.handleTruckStories();
    
  }

  @Test
  public void testKeepsFutureEventWhenNoOverlap() {
    expectMatched(false);
    expectOffTheRoad(false);
    TruckStop stopAfter =
        TruckStop.builder().truck(truck2).startTime(matchEndTime.plusHours(1).toDateTime()).endTime(matchEndTime.plusHours(2).toDateTime()).location(loca).build();
    when(truckStopDAO.findDuring(TRUCK_2_ID, currentDay))
        .thenReturn(ImmutableList.<TruckStop>of(stopAfter));
    expectTweetsIgnored();
    truckStopDAO.addStops(ImmutableList.<TruckStop>of(matchedStop));
    
    service.handleTruckStories();
    
  }

  @Test
  public void testObserverTwittalyzerNoTweets() {
    Location uchicago = Location.builder().lat(-234).lng(-432).name("University of Chicago").build();
    when(truckObserverDAO.findAll()).thenReturn(ImmutableList.of(
        new TruckObserver("uchinomgo", uchicago, ImmutableList.of("breakfast", "lunch")),
        new TruckObserver("mdw2mnl", uchicago, ImmutableList.of("breakfast", "lunch"))));
    when(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false)).thenReturn(ImmutableList.<Story>of());
    when(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false)).thenReturn(ImmutableList.<Story>of());
    
    service.observerAnalyze();
    
  }

  @Test
  public void testObserverTwittalyzerTweetsWithNoHashTag() {
    Location uchicago = Location.builder().lat(-234).lng(-432).name("University of Chicago").build();
    when(truckObserverDAO.findAll()).thenReturn(ImmutableList.of(
        new TruckObserver("uchinomgo", uchicago, ImmutableList.of("breakfast", "lunch")),
        new TruckObserver("mdw2mnl", uchicago, ImmutableList.of("breakfast", "lunch"))));
    Story tweet1 = new Story.Builder().userId("uchinomgo").ignoreInTwittalyzer(false)
        .text("Today we have these food trucks: @CaponiesExp @threejsbbq").build();
    when(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false))
        .thenReturn(ImmutableList.<Story>of(tweet1));
    when(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false)).thenReturn(ImmutableList.<Story>of());
    tweetDAO.save(ImmutableList.of(tweet1));
    
    service.observerAnalyze();
    
  }

  @Test
  public void testObserverTwittalyzerTweetsNoExistingStops() {
    Location uchicago = Location.builder()
        .lat(-40)
        .lng(-80)
        .name("University of Chicago")
        .build();
    when(truckObserverDAO.findAll()).thenReturn(ImmutableList.of(
        new TruckObserver("uchinomgo", uchicago, ImmutableList.of("breakfast", "lunch")),
        new TruckObserver("mdw2mnl", uchicago, ImmutableList.of("breakfast", "lunch"))));
    Story tweet1 = new Story.Builder().userId("uchinomgo").ignoreInTwittalyzer(false)
        .text("breakfast: @CaponiesExp @threejsbbq @somethingelse").build();
    truck1 = Truck.builder(truck1).categories(ImmutableSet.of("Breakfast")).twitterHandle("caponiesexp").id("caponiesexp").build();
    truck2 = Truck.builder(truck1).categories(ImmutableSet.of("Breakfast")).twitterHandle("threejsbbq").id("threejsbbq").build();
    when(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false))
        .thenReturn(ImmutableList.<Story>of(tweet1));
    when(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false)).thenReturn(ImmutableList.<Story>of());
    when(truckDAO.findByTwitterId("caponiesexp")).thenReturn(ImmutableList.of(truck1));
    when(truckStopDAO.findDuring("caponiesexp", currentDay)).thenReturn(ImmutableList.<TruckStop>of());
    when(truckDAO.findByTwitterId("threejsbbq")).thenReturn(ImmutableList.of(truck2));
    when(truckStopDAO.findDuring("threejsbbq", currentDay)).thenReturn(ImmutableList.<TruckStop>of(matchedStop));
    when(truckDAO.findByTwitterId("somethingelse")).thenReturn(ImmutableList.<Truck>of());
    truckStopDAO.addStops(ImmutableList.of(TruckStop.builder()
        .truck(truck1)
        .startTime(now)
        .endTime(now.plusHours(2))
        .location(uchicago)
        .build()));
    emailNotifier.systemNotifyTrucksAddedByObserver(ImmutableMap.of(truck1, tweet1));
    tweetDAO.save(ImmutableList.of(tweet1));
    
    service.observerAnalyze();
    
  }

  @Test
  public void testMultiple() {
    Location uchicago = Location.builder()
        .lat(-40)
        .lng(-80)
        .name("University of Chicago")
        .build();
    when(truckObserverDAO.findAll()).thenReturn(ImmutableList.of(
        new TruckObserver("uchinomgo", uchicago, ImmutableList.of("breakfast", "#foodtrucks")),
        new TruckObserver("mdw2mnl", uchicago, ImmutableList.of("breakfast", "lunch"))));
    Story tweet1 = new Story.Builder().userId("uchinomgo").ignoreInTwittalyzer(false)
        .text("we have these #foodtrucks: @CaponiesExp @threejsbbq @somethingelse").build();
    Story tweet2 = new Story.Builder().userId("mdw2mnl").ignoreInTwittalyzer(false)
        .text("we have these #foodtrucks: @CaponiesExp").build();
    truck1 = Truck.builder(truck1).categories(ImmutableSet.of("Breakfast")).twitterHandle("caponiesexp").id("caponiesexp").build();
    truck2 = Truck.builder(truck1).categories(ImmutableSet.of("Breakfast")).twitterHandle("threejsbbq").id("threejsbbq").build();
    when(tweetDAO.findTweetsAfter(now.minusHours(6), "uchinomgo", false))
        .thenReturn(ImmutableList.<Story>of(tweet1));
    when(tweetDAO.findTweetsAfter(now.minusHours(6), "mdw2mnl", false))
        .thenReturn(ImmutableList.<Story>of(tweet2));
    when(truckDAO.findByTwitterId("caponiesexp")).thenReturn(ImmutableList.of(truck1));
    when(truckStopDAO.findDuring("caponiesexp", currentDay)).thenReturn(ImmutableList.<TruckStop>of());
    when(truckDAO.findByTwitterId("threejsbbq")).thenReturn(ImmutableList.of(truck2));
    when(truckStopDAO.findDuring("threejsbbq", currentDay)).thenReturn(ImmutableList.<TruckStop>of(matchedStop));
    when(truckDAO.findByTwitterId("somethingelse")).thenReturn(ImmutableList.<Truck>of());
    truckStopDAO.addStops(ImmutableList.of(TruckStop.builder()
        .truck(truck1)
        .startTime(now)
        .endTime(now.plusHours(2))
        .location(uchicago)
        .build()));
    emailNotifier.systemNotifyTrucksAddedByObserver(ImmutableMap.of(truck1, tweet1));
    tweetDAO.save(ImmutableList.of(tweet1));
    tweetDAO.save(ImmutableList.of(tweet2));
    
    service.observerAnalyze();
    
  }

  @Test
  public void testHandleNotificationsForMentionedTrucksNoneMentioned() {
    final TruckStopMatch matched = TruckStopMatch.builder()
        .stop(matchedStop)
        .story(Story.builder().text(basicTweet.getText()).build())
            .build();
    
    service.handleAdditionalTrucks(matchedStop, matched);
    
  }

  @Test
  public void testHandleNotificationsForMentionedTrucksMentionsNonTruck() {
    basicTweet = Story.builder(basicTweet).text("We are here at @foobar on Clark and Monroe").build();
    final TruckStopMatch matched = TruckStopMatch.builder()
        .stop(matchedStop)
        .story(Story.builder().text(basicTweet.getText()).build())
            .build();
    when(truckDAO.findByTwitterId("foobar")).thenReturn(ImmutableSet.<Truck>of());
    
    service.handleAdditionalTrucks(matchedStop, matched);
    
  }

  @Test
  public void testHandleNotificationsForMentionedTrucksMentionsTruck() {
    basicTweet = Story.builder(basicTweet).text("We are here at @truck1 on Clark and Monroe").build();
    final TruckStopMatch matched = TruckStopMatch.builder()
        .stop(matchedStop)
        .story(Story.builder().text(basicTweet.getText()).build())
            .build();
    when(truckDAO.findByTwitterId("truck1")).thenReturn(ImmutableSet.of(truck1));
    when(truckStopDAO.findOverRange("truck1", matchedStop.getInterval()))
        .thenReturn(ImmutableList.<TruckStop>of());
    emailNotifier.notifyAddMentionedTrucks(ImmutableSet.of("truck1"), matchedStop, basicTweet.getText());
    
    service.handleAdditionalTrucks(matchedStop, matched);
    
  }
}
