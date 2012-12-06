package foodtruck.notifications;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gdata.util.common.base.Joiner;
import com.google.inject.Inject;

import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.model.Truck;
import foodtruck.model.TwitterNotificationAccount;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * @author aviolette
 * @since 12/3/12
 */
public class NotificationServiceImpl implements NotificationService {
  private static final Logger log = Logger.getLogger(NotificationServiceImpl.class.getName());
  private final FoodTruckStopService truckService;
  private final Clock clock;
  private final TwitterNotificationAccountDAO notificationAccountDAO;

  @Inject
  public NotificationServiceImpl(FoodTruckStopService truckService, Clock clock,
      TwitterNotificationAccountDAO notificationAccountDAO) {
    this.truckService = truckService;
    this.clock = clock;
    this.notificationAccountDAO = notificationAccountDAO;
  }

  @Override public void sendNotifications() {
    for (TwitterNotificationAccount account : findTwitterNotificationAccounts()) {
      try {
        Set<Truck> trucks = truckService.findTrucksAtLocation(clock.currentDay(), account.getLocation());
        if (trucks.isEmpty()) {
          updateStatus(account, String.format("No trucks at %s today", account.getLocation().getName()));
        } else {
          Joiner joiner = Joiner.on(" ");
          Iterable<String> twitterHandles = Iterables.transform(trucks, new Function<Truck, String>(){
            @Override public String apply(Truck input) {
              return "@" + input.getTwitterHandle();
            }
          });
          updateStatus(account, String.format("Trucks at %s today: %s ", account.getName(), joiner.join(twitterHandles)));
        }
      } catch (Exception e) {
        log.log(Level.WARNING, "An exception occurred", e);
      }
    }
  }

  private void updateStatus(TwitterNotificationAccount account, String status) throws TwitterException {
    log.info("Sending status: " + status);
    Twitter twitter = new TwitterFactory(account.twitterCredentials()).getInstance();
    twitter.updateStatus(status);
  }

  private Iterable<TwitterNotificationAccount> findTwitterNotificationAccounts() {
    return notificationAccountDAO.findAll();
  }
}
