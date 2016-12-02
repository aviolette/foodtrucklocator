package foodtruck.notifications;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.model.StaticConfig;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.time.Clock;

import static org.junit.Assert.assertEquals;

/**
 * @author aviolette
 * @since 12/6/12
 */
public class TwitterPublicEventNotificationServiceTest {

  private TwitterNotificationAccountDAO notificationDAO;
  private Clock clock;
  private FoodTruckStopService truckService;
  private TwitterEventNotificationService service;
  private LocationDAO locationDAO;

  @Before
  public void before() {
    truckService = null;
    clock = null;
    notificationDAO = null;
    locationDAO = null;
    service = new TwitterEventNotificationService(truckService, clock, notificationDAO, null, new StaticConfig(),
        locationDAO, null);
  }

  @Test
  public void splitLessThan140() {
    String foo = "This is foo bar @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234";
    List<String> notifications = service.twitterSplitter(null, foo);
    assertEquals(1, notifications.size());
    assertEquals(foo, notifications.get(0));
  }

  @Test
  public void splitExactly140() {
    String foo = "0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456";
    List<String> notifications = service.twitterSplitter(null, foo);
    assertEquals(1, notifications.size());
    assertEquals(foo, notifications.get(0));
  }

  @Test
  public void splitLessGreater140() {
    String foo =
"Trucks at 600W @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abc1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @splitsonthis @abc1234 " +
        "@abcd1234 @abcd1234";
    List<String> notifications = service.twitterSplitter("600 West", foo);
    assertEquals(2, notifications.size());
    assertEquals("Additional trucks at 600 West: @splitsonthis @abc1234 @abcd1234 @abcd1234", notifications.get(0));
    assertEquals("Trucks at 600W @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abc1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234", notifications.get(1));
  }
}
