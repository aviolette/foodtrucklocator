package foodtruck.notifications;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.api.client.util.Strings;
import com.google.inject.Inject;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.RetweetsDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.Story;
import foodtruck.model.StoryType;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.model.TwitterNotificationAccount;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.socialmedia.MessageSplitter;
import foodtruck.socialmedia.NoSplitSplitter;
import foodtruck.socialmedia.TwitterConnector;
import foodtruck.time.Clock;
import foodtruck.util.ServiceException;

/**
 * @author aviolette
 * @since 12/3/12
 */
class TwitterEventNotificationService implements PublicEventNotificationService {
  private static final Logger log = Logger.getLogger(TwitterEventNotificationService.class.getName());
  private final FoodTruckStopService truckService;
  private final Clock clock;
  private final TwitterNotificationAccountDAO notificationAccountDAO;
  private final RetweetsDAO retweetsDAO;
  private final StaticConfig config;
  private final LocationDAO locationDAO;
  private final TwitterConnector twitterConnector;
  static final MessageSplitter NO_SPLIT_SPLITTER = new NoSplitSplitter();

  @Inject
  public TwitterEventNotificationService(FoodTruckStopService truckService, Clock clock,
      TwitterNotificationAccountDAO notificationAccountDAO, RetweetsDAO retweetsDAO, StaticConfig config,
      LocationDAO locationDAO, TwitterConnector twitterConnector) {
    this.truckService = truckService;
    this.clock = clock;
    this.notificationAccountDAO = notificationAccountDAO;
    this.retweetsDAO = retweetsDAO;
    this.config = config;
    this.locationDAO = locationDAO;
    this.twitterConnector = twitterConnector;
  }

  @Override
  public void sendLunchtimeNotifications() {
    for (TwitterNotificationAccount account : notificationAccountDAO.findAll()) {
      if (!account.isActive()) {
        log.log(Level.FINE, "Skipping notifications for {0} because it is not active", account.getName());
        continue;
      }
      try {
        Set<Truck> trucks = truckService.findTrucksNearLocation(account.getLocation(), clock.now());
        if (!trucks.isEmpty()) {
          String format = String.format("Trucks at %s today: %s ", account.getName(),
              trucks.stream()
                  .map(Truck::nameForTwitterDisplay)
                  .collect(Collectors.joining(" ")));
          // include base URL in tweet (when possible) to drive web traffic
          String potential = format.trim() + "\n\n" + config.getBaseUrl();
          if (potential.length() <= 140) {
            format = potential;
          }
          twitterConnector.sendStatusFor(format, account, new TruckStopSplitter(account.getName()));
        }
      } catch (Exception e) {
        log.log(Level.WARNING, "An exception occurred", e);
      }
    }
  }

  @Override
  public void updateLocationInNotifications(Location location) {
    TwitterNotificationAccount account = notificationAccountDAO.findByLocationName(location.getName());
    if (account == null) {
      return;
    }
    account = TwitterNotificationAccount.builder(account)
        .location(location)
        .build();
    notificationAccountDAO.save(account);
  }

  @Override
  public void notifyStopStart(TruckStop truckStop) {
    Truck truck = truckStop.getTruck();
    if (truck.isPostAtNewStop() && truck.getHasTwitterCredentials()) {
      Location location = truckStop.getLocation();
      twitterConnector.sendStatusFor(String.format("We are now at %s. %s", location.getShortenedName(),
          config.getBaseUrl() + "/locations/" + location.getKey()), truck, NO_SPLIT_SPLITTER);
    } else {
      messageAtStop(truckStop, "%s is now at %s %s");
    }
  }

  @Override
  public void notifyStopEnd(TruckStop truckStop) {
    messageAtStop(truckStop, "%s is leaving %s %s");
  }

  private void messageAtStop(TruckStop truckStop, String messageFormat) {
    for (TwitterNotificationAccount account : notificationAccountDAO.findAll()) {
      Truck truck = truckStop.getTruck();
      Location location = truckStop.getLocation();
      location = locationDAO.findByName(location.getName()).orElse(location);
      if (location.containedWithRadiusOf(account.getLocation()) && !retweetsDAO.hasBeenRetweeted(truck.getId(),
          account.getTwitterHandle())) {
        String descriptor = Strings.isNullOrEmpty(
            truck.getTwitterHandle()) ? truck.getName() : ". @" + truck.getTwitterHandle();
        twitterConnector.sendStatusFor(String.format(messageFormat, descriptor, location.getShortenedName(),
            config.getBaseUrl() + "/locations/" + location.getKey()), account, NO_SPLIT_SPLITTER);
        retweetsDAO.markRetweeted(truck.getId(), account.getTwitterHandle());
      }
    }
  }

  @Override
  public void share(Story story, TruckStop stop) {
    if (story.getStoryType() != StoryType.TWEET) {
      return;
    }
    try {
      log.log(Level.INFO, "Checking for retweets against {0} {1}", new Object[]{stop, story});
      for (TwitterNotificationAccount account : notificationAccountDAO.findAll()) {
        if (retweetsDAO.hasBeenRetweeted(stop.getTruck()
            .getId(), account.getTwitterHandle())) {
          log.log(Level.INFO, "Already retweeted at {0} {1}",
              new Object[]{stop.getTruck().getId(), account.getTwitterHandle()});
          continue;
        }
        if (stop.getLocation()
            .containedWithRadiusOf(account.getLocation())) {
          log.log(Level.INFO, "RETWEETING:" + story.getText());
          retweetsDAO.markRetweeted(stop.getTruck()
              .getId(), account.getTwitterHandle());
          try {
            twitterConnector.retweet(story.getId(), account);
          } catch (ServiceException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
          }
        } else {
          log.log(Level.INFO, "{0} not contained within radius of {1}",
              new Object[]{stop.getLocation(), account.getLocation()});
        }
      }
    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override
  public void retweet(String accountName, String tweetIdValue) {
    TwitterNotificationAccount account = notificationAccountDAO.findByTwitterHandle(accountName);
    if (account == null) {
      throw new WebApplicationException(Response.status(400)
          .entity("Account not found: " + accountName)
          .build());
    }
    final long tweetId = Long.parseLong(tweetIdValue);
    twitterConnector.retweet(tweetId, account);
  }
}
