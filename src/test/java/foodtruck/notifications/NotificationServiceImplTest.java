package foodtruck.notifications;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.RetweetsDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import static org.junit.Assert.assertEquals;

/**
 * @author aviolette
 * @since 12/6/12
 */
public class NotificationServiceImplTest  {

  private TwitterNotificationAccountDAO notificationDAO;
  private Clock clock;
  private FoodTruckStopService truckService;
  private NotificationServiceImpl service;
  private RetweetsDAO retweetsDAO;
  private ConfigurationDAO configurationDAO;

  @Before
  public void before() {
    truckService = null; clock = null; notificationDAO = null; retweetsDAO = null;
    configurationDAO = null;
    service = new NotificationServiceImpl(truckService, clock, notificationDAO, retweetsDAO, configurationDAO);
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
