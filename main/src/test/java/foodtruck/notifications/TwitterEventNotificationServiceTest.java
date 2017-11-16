package foodtruck.notifications;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.RetweetsDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.model.StaticConfig;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.model.TwitterNotificationAccount;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.schedule.ModelTestHelper;
import foodtruck.socialmedia.TwitterConnector;
import foodtruck.time.Clock;

import static foodtruck.notifications.TwitterEventNotificationService.NO_SPLIT_SPLITTER;
import static foodtruck.schedule.ModelTestHelper.aonTwitterAccount;
import static foodtruck.schedule.ModelTestHelper.clarkAndMonroeTwitterAccount;
import static foodtruck.schedule.ModelTestHelper.truck1;
import static foodtruck.schedule.ModelTestHelper.wackerAndAdams;
import static foodtruck.schedule.ModelTestHelper.wackerAndAdamsTwitterAccount;

/**
 * @author aviolette
 * @since 11/14/17
 */
@RunWith(MockitoJUnitRunner.class)
public class TwitterEventNotificationServiceTest extends Mockito {

  private @Mock FoodTruckStopService truckService;
  private @Mock Clock clock;
  private @Mock TwitterNotificationAccountDAO notificationAccountDAO;
  private @Mock RetweetsDAO retweetsDAO;
  private @Mock StaticConfig config;
  private @Mock LocationDAO locationDAO;
  private @Mock TwitterConnector connector;
  private TwitterEventNotificationService service;
  private static final DateTime NOW = new DateTime(2017, 10, 10, 10, 10, 10);

  @Before
  public void setup() {
    service = new TwitterEventNotificationService(truckService, clock, notificationAccountDAO,
        retweetsDAO, config, locationDAO, connector);
    when(config.getBaseUrl()).thenReturn("http://foo");
  }

  @Test
  public void sendLunchtimeNotifications() {
    TwitterNotificationAccount account1 = wackerAndAdamsTwitterAccount();
    TwitterNotificationAccount account2 = aonTwitterAccount();
    when(config.getBaseUrl()).thenReturn("http://foo");
    when(clock.now()).thenReturn(NOW);
    when(notificationAccountDAO.findAll()).thenReturn(
        ImmutableList.of(clarkAndMonroeTwitterAccount(), account1, account2));
    when(truckService.findTrucksNearLocation(wackerAndAdams(), NOW)).thenReturn(
        ImmutableSet.of(truck1(), ModelTestHelper.truck2()));
    when(truckService.findTrucksNearLocation(ModelTestHelper.aon(), NOW)).thenReturn(
        ImmutableSet.of(ModelTestHelper.truck3(), ModelTestHelper.truck4()));
    service.sendLunchtimeNotifications();
    verify(connector).sendStatusFor(
        "Trucks at Wacker and Adams today: @truck1 @truck2\n\nhttp://foo", account1,
        new TruckStopSplitter(account1.getName()));
    verify(connector).sendStatusFor(
        "Trucks at AON today: @truck3 @truck4\n" + "\n" + "http://foo", account2,
        new TruckStopSplitter(account2.getName()));
  }

  @Test
  public void notifyStart() {
    Truck truck = Truck.builder(truck1())
        .twitterToken("foo")
        .twitterTokenSecret("hoos")
        .postAtNewStop(true)
        .build();
    TruckStop stop = TruckStop.builder()
        .startTime(NOW.plusHours(1))
        .endTime(NOW.plusHours(5))
        .location(ModelTestHelper.clarkAndMonroe())
        .truck(truck)
        .build();
    service.notifyStopStart(stop);
    verify(connector).sendStatusFor("We are now at Clark and Monroe. http://foo/locations/1234",
        truck, NO_SPLIT_SPLITTER);
  }

  // If the truck has "Post at New Stop" off, then post with the Location-specific account instead
  @Test
  public void notifyStart_postWithLocationAccount() {
    Truck truck = Truck.builder(truck1())
        .twitterToken("foo")
        .twitterTokenSecret("hoos")
        .postAtNewStop(false)
        .build();
    TruckStop stop = TruckStop.builder()
        .startTime(NOW.plusHours(1))
        .endTime(NOW.plusHours(5))
        .location(wackerAndAdams())
        .truck(truck)
        .build();
    TwitterNotificationAccount wa = wackerAndAdamsTwitterAccount();
    when(notificationAccountDAO.findAll()).thenReturn(ImmutableList.of(wa, clarkAndMonroeTwitterAccount()));
    when(locationDAO.findByName(wackerAndAdams().getName())).thenReturn(Optional.of(wackerAndAdams()));
    service.notifyStopStart(stop);
    verify(connector).sendStatusFor(
        ". @truck1 is now at Wacker and Adams http://foo/locations/456", wa, NO_SPLIT_SPLITTER);
  }
}