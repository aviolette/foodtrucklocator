package foodtruck.notifications;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.api.client.util.Strings;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
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
import foodtruck.socialmedia.TwitterFactoryWrapper;
import foodtruck.time.Clock;
import foodtruck.truckstops.FoodTruckStopService;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.PropertyConfiguration;

/**
 * @author aviolette
 * @since 12/3/12
 */
public class TwitterEventNotificationService implements PublicEventNotificationService {
  private static final Joiner HANDLE_JOINER = Joiner.on(" ")
      .skipNulls();
  private static final Logger log = Logger.getLogger(TwitterEventNotificationService.class.getName());
  private final FoodTruckStopService truckService;
  private final Clock clock;
  private final TwitterNotificationAccountDAO notificationAccountDAO;
  private final RetweetsDAO retweetsDAO;
  private final StaticConfig config;
  private final LocationDAO locationDAO;
  private final TwitterFactoryWrapper twitterFactoryWrapper;

  @Inject
  public TwitterEventNotificationService(FoodTruckStopService truckService, Clock clock,
      TwitterNotificationAccountDAO notificationAccountDAO, RetweetsDAO retweetsDAO, StaticConfig config,
      LocationDAO locationDAO, TwitterFactoryWrapper twitterFactoryWrapper) {
    this.truckService = truckService;
    this.clock = clock;
    this.notificationAccountDAO = notificationAccountDAO;
    this.retweetsDAO = retweetsDAO;
    this.config = config;
    this.locationDAO = locationDAO;
    this.twitterFactoryWrapper = twitterFactoryWrapper;
  }

  @Override
  public void sendLunchtimeNotifications() {
    for (TwitterNotificationAccount account : findTwitterNotificationAccounts()) {
      if (!account.isActive()) {
        log.log(Level.FINE, "Skipping notifications for {0} because it is not active", account.getName());
        continue;
      }
      try {
        Set<Truck> trucks = truckService.findTrucksNearLocation(account.getLocation(), clock.now());
        if (!trucks.isEmpty()) {
          updateStatus(account, String.format("Trucks at %s today: %s ", account.getName(), FluentIterable.from(trucks)
              .transform(new Function<Truck, String>() {
                public String apply(Truck input) {
                  if (Strings.isNullOrEmpty(input.getTwitterHandle())) {
                    return input.getName();
                  }
                  return "@" + input.getTwitterHandle();
                }
              })
              .join(HANDLE_JOINER)));
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
      Twitter twitter = twitterFactoryWrapper.createDetached(twitterAccessToken(truck));
      try {
        twitter.updateStatus(String.format("We are now at %s. %s", location.getShortenedName(),
            config.getBaseUrl() + "/locations/" + location.getKey()));
      } catch (TwitterException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    } else {
      messageAtStop(truckStop, "%s is now at %s %s");
    }
  }

  @Nullable
  private AccessToken twitterAccessToken(Truck truck) {
    if (truck.getHasTwitterCredentials()) {
      return new AccessToken(truck.getTwitterToken(), truck.getTwitterTokenSecret());
    }
    return null;
  }

  @Override
  public void notifyStopEnd(TruckStop truckStop) {
    messageAtStop(truckStop, "%s is leaving %s %s");
  }

  private void messageAtStop(TruckStop truckStop, String messageFormat) {
    for (TwitterNotificationAccount account : notificationAccountDAO.findAll()) {
      Truck truck = truckStop.getTruck();
      Location location = truckStop.getLocation();
      location = MoreObjects.firstNonNull(locationDAO.findByAddress(location.getName()), null);
      if (location.containedWithRadiusOf(account.getLocation()) && !retweetsDAO.hasBeenRetweeted(truck.getId(),
          account.getTwitterHandle())) {
        Twitter twitter = new TwitterFactory(twitterCredentials(account)).getInstance();
        try {
          String descriptor = Strings.isNullOrEmpty(
              truck.getTwitterHandle()) ? truck.getName() : ". @" + truck.getTwitterHandle();
          twitter.updateStatus(String.format(messageFormat, descriptor, location.getShortenedName(),
              config.getBaseUrl() + "/locations/" + location.getKey()));
          retweetsDAO.markRetweeted(truck.getId(), account.getTwitterHandle());
        } catch (TwitterException e) {
          log.log(Level.SEVERE, e.getMessage(), e);
        }
      }
    }
  }

  private PropertyConfiguration twitterCredentials(TwitterNotificationAccount account) {
    Properties properties = new Properties();
    InputStream in = Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream("twitter4j.properties");
    try {
      properties.load(in);
      in.close();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    properties.put(PropertyConfiguration.OAUTH_ACCESS_TOKEN, account.getOauthToken());
    properties.put(PropertyConfiguration.OAUTH_ACCESS_TOKEN_SECRET, account.getOauthTokenSecret());
    return new PropertyConfiguration(properties);
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
          Twitter twitter = new TwitterFactory(twitterCredentials(account)).getInstance();
          try {
            log.log(Level.INFO, "RETWEETING:" + story.getText());
            retweetsDAO.markRetweeted(stop.getTruck()
                .getId(), account.getTwitterHandle());
            twitter.retweetStatus(story.getId());
          } catch (TwitterException e) {
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
    try {
      TwitterNotificationAccount account = notificationAccountDAO.findByTwitterHandle(accountName);
      if (account == null) {
        throw new WebApplicationException(Response.status(400)
            .entity("Account not found: " + accountName)
            .build());
      }
      Twitter twitter = new TwitterFactory(twitterCredentials(account)).getInstance();
      final long tweetId = Long.parseLong(tweetIdValue);
      twitter.retweetStatus(tweetId);
    } catch (TwitterException e) {
      throw Throwables.propagate(e);
    }
  }

  @VisibleForTesting
  List<String> twitterSplitter(String name, String status) {
    ImmutableList.Builder<String> statuses = ImmutableList.builder();
    int start = 0, end = 140, cutoff = 140;
    // TODO: This doesn't work when greater than 280
    while (true) {
      int min = Math.min(status.substring(start)
          .length(), end);
      String chunk = status.substring(start, start + min);
      if (start != 0) {
        chunk = "Additional trucks at " + name + ":" + chunk;
      }
      if (min < cutoff) {
        statuses.add(chunk);
        break;
      } else {
        end = status.substring(0, end)
            .lastIndexOf(' ');
        statuses.add(status.substring(start, end));
        start = end;
        end = end + 140;
      }
    }
    return statuses.build()
        .reverse();
  }

  private void updateStatus(TwitterNotificationAccount account, String status) throws TwitterException {
    log.log(Level.INFO, "Initial status: {0}", new Object[]{status});
    Twitter twitter = new TwitterFactory(twitterCredentials(account)).getInstance();
    for (String theStatus : twitterSplitter(account.getName(), status)) {
      log.log(Level.INFO, "Sending status update for account {0}: {1}", new Object[]{account.getName(), theStatus});
      twitter.updateStatus(theStatus);
    }
  }

  private Iterable<TwitterNotificationAccount> findTwitterNotificationAccounts() {
    return notificationAccountDAO.findAll();
  }
}
