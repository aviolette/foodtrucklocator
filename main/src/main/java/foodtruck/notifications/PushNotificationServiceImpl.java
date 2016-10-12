package foodtruck.notifications;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.util.Strings;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.NotificationDeviceProfileDAO;
import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.model.NotificationDeviceProfile;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.DateOnlyFormatter;

/**
 * @author aviolette
 * @since 2/14/16
 */
public class PushNotificationServiceImpl implements PushNotificationService {
  private static final Logger log = Logger.getLogger(PushNotificationServiceImpl.class.getName());
  private final NotificationDeviceProfileDAO notificationDAO;
  private final FoodTruckStopService stopService;
  private final Clock clock;
  private final Set<NotificationProcessor> processors;
  private final LocationDAO locationDAO;
  private final MemcacheService memcacheService;
  private final DateTimeFormatter formatter;

  @Inject
  public PushNotificationServiceImpl(NotificationDeviceProfileDAO notificationDAO,
      FoodTruckStopService stopService, Clock clock, Set<NotificationProcessor> processors,
      LocationDAO locationDAO, MemcacheService memcacheService,
      @DateOnlyFormatter DateTimeFormatter formatter) {
    this.notificationDAO = notificationDAO;
    this.stopService = stopService;
    this.clock = clock;
    this.processors = processors;
    this.locationDAO = locationDAO;
    this.memcacheService = memcacheService;
    this.formatter = formatter;
  }

  @Override
  public void sendPushNotifications() {
    // TODO: probably should be launched in a task queue
    DateTime now = clock.now();
    DailySchedule schedule = stopService.findStopsForDayAfter(now);
    final Multimap<String, Truck> locationsToTrucks = HashMultimap.create();
    ImmutableSet.Builder<Location> activeLocations = ImmutableSet.builder();
    for (TruckStop input : schedule.activeStopsAtLocation(now)) {
      locationsToTrucks.put(input.getLocation().getName(), input.getTruck());
      activeLocations.add(input.getLocation());
    }
    cycleDeviceProfiles(activeLocations.build(), locationsToTrucks);
  }
  // TODO: filter by truck as well
  // TODO: handle radii

  private void cycleDeviceProfiles(ImmutableSet<Location> activeLocations,
      Multimap<String, Truck> locationsToTrucks) {
    final LocalDate today = clock.currentDay();
    LoadingCache<String, Location> locationCache = CacheBuilder.newBuilder()
        .maximumSize(200)
        .build(new CacheLoader<String, Location>() {
          public Location load(String key) throws Exception {
            return locationDAO.findByAddress(key);
          }
        });
    for (final NotificationDeviceProfile deviceProfile : notificationDAO.findAll()) {
      try {
        for (final Location location : activeLocations) {
          Location stopLocation = locationCache.get(location.getName());
          for (String locationName : deviceProfile.getLocationNames()) {
            Location resolvedLocation = locationCache.get(locationName);
            if (stopLocation.containedWithRadiusOf(resolvedLocation)) {
              enqueue(locationsToTrucks, today, deviceProfile, location, resolvedLocation);
              break;
            }
          }
        }
      } catch (ExecutionException e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }

  private void enqueue(Multimap<String, Truck> locationsToTrucks, final LocalDate today,
      final NotificationDeviceProfile deviceProfile, Location stopLocation,
      final Location resolvedLocation) {
    // TODO: put on queue
    StringBuilder messageBuilder = new StringBuilder("New stops @ ");
    messageBuilder.append(stopLocation.getName().replaceAll(", Chicago, IL", ""));
    String trucksAtLocation = FluentIterable.from(locationsToTrucks.get(stopLocation.getName()))
        .filter(new Predicate<Truck>() {
          public boolean apply(Truck input) {
            String throttleKey = throttleKey(deviceProfile.getDeviceToken(), input.getId(),
                (long) resolvedLocation.getKey(), today);
            Object throttle = memcacheService.get(throttleKey);
            if (throttle == null) {
              memcacheService.put(throttleKey, "hit",
                  Expiration.onDate(today.plusDays(1).toDateTimeAtStartOfDay().toDate()));
            }
            return throttle == null;
          }
        })
        .transform(new Function<Truck, String>() {
          public String apply(Truck input) {
            return input.getName();
          }
        })
        .join(Joiner.on(", "));
    if (!Strings.isNullOrEmpty(trucksAtLocation)) {
      messageBuilder.append(": ").append(trucksAtLocation);
      for (NotificationProcessor processor : processors) {
        processor.handle(new PushNotification("New trucks at " + stopLocation.getName().replace(", Chicago, IL", ""),
            messageBuilder.toString(),
            deviceProfile.getDeviceToken(),
            deviceProfile.getType()));
      }
    }
  }

  @Override
  public void register(NotificationDeviceProfile profile) {
    notificationDAO.save(profile);
  }

  @Override
  public void deregister(String deviceToken) {
    notificationDAO.delete(deviceToken);
  }

  public String throttleKey(String deviceToken, String truckId, long locationId, LocalDate day) {
    return "throttle-" + formatter.print(day) + "-" + deviceToken + "-" + truckId + "-" + locationId;
  }
}
