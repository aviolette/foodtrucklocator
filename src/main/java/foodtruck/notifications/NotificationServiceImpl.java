package foodtruck.notifications;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gdata.util.common.base.Joiner;
import com.google.inject.Inject;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.RetweetsDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.model.Location;
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
  private final RetweetsDAO retweetsDAO;
  private final ConfigurationDAO configurationDAO;

  @Inject
  public NotificationServiceImpl(FoodTruckStopService truckService, Clock clock,
      TwitterNotificationAccountDAO notificationAccountDAO, RetweetsDAO retweetsDAO,
      ConfigurationDAO configurationDAO) {
    this.truckService = truckService;
    this.clock = clock;
    this.notificationAccountDAO = notificationAccountDAO;
    this.retweetsDAO = retweetsDAO;
    this.configurationDAO = configurationDAO;
  }

  @Override public void sendNotifications() {
    boolean updateIfNoneFound = configurationDAO.find().isSendNotificationTweetWhenNoTrucks();
    for (TwitterNotificationAccount account : findTwitterNotificationAccounts()) {
      if (!account.isActive()) {
        log.log(Level.INFO, "Skipping notifications for {0} because it is not active", account.getName());
        continue;
      }
      try {
        Set<Truck> trucks = truckService.findTrucksNearLocation(account.getLocation(), clock.now());
        if (trucks.isEmpty()) {
          if (updateIfNoneFound) {
            updateStatus(account, String.format("No trucks at %s today", account.getName()));
          }
        } else {
          for (Truck truck : trucks) {
            retweetsDAO.markRetweeted(truck.getId(), account.getTwitterHandle());
          }
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

  @Override public void updateLocationInNotifications(Location location) {
    TwitterNotificationAccount account = notificationAccountDAO.findByLocationName(location.getName());
    if (account == null) {
      return;
    }
    account = TwitterNotificationAccount.builder(account).location(location).build();
    notificationAccountDAO.save(account);
  }

  @VisibleForTesting
  List<String> twitterSplitter(String name, String status) {
    ImmutableList.Builder<String> statuses = ImmutableList.builder();
    int start = 0, end = 140, cutoff = 140;
    // TODO: This doesn't work when greater than 280
    while (true) {
      int min = Math.min(status.substring(start).length(), end);
      String chunk = status.substring(start, start+min);
      if (start != 0) {
        chunk = "Additional trucks at " + name + ":" + chunk;
      }
      if (min < cutoff) {
        statuses.add(chunk);
        break;
      } else {
        end = status.substring(0, end).lastIndexOf(' ');
        statuses.add(status.substring(start, end));
        start = end;
        end = end + 140;
      }
    }
    return statuses.build().reverse();
  }

  private void updateStatus(TwitterNotificationAccount account, String status) throws TwitterException {
    log.log(Level.INFO, "Initial status: {0}", new Object[] { status});
    Twitter twitter = new TwitterFactory(account.twitterCredentials()).getInstance();
    for (String theStatus : twitterSplitter(account.getName(), status )) {
      log.log(Level.INFO, "Sending status update for account {0}: {1}", new Object[] { account.getName(), theStatus } );
      twitter.updateStatus(theStatus);
    }
  }

  private Iterable<TwitterNotificationAccount> findTwitterNotificationAccounts() {
    return notificationAccountDAO.findAll();
  }
}
