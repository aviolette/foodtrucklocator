package foodtruck.notifications;

import java.util.Set;

import com.google.api.client.util.Strings;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
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
    final LocalDate today = clock.currentDay();
    // TODO: add the memcached fronted schedule cacher log from FrontPageServlet into the service
    DailySchedule schedule = stopService.findStopsForDayAfter(now);
    // locationNames to trucks
    final Multimap<String, Truck> locationsToTrucks = HashMultimap.create();
    Set<Location> activeLocations = FluentIterable.from(schedule.activeStopsAtLocation(now))
        .transform(new Function<TruckStop, Location>() {
          public Location apply(TruckStop input) {
            ///TODO: side-effect...clean this up later
            locationsToTrucks.put(input.getLocation().getName(), input.getTruck());
            return input.getLocation();
          }
        })
        .toSet();

    for (final NotificationDeviceProfile deviceProfile : notificationDAO.findAll()) {
      // TODO: add throttle so same device hasn't been triggered twice for same criteria
      // TODO: filter by truck as well

      for (final Location location : activeLocations) {
        // TODO: handle radiuses
        // TODO: cache locations so we don't have to keep looking them up (or use memcached locationDAO)

        for (String locationName : deviceProfile.getLocationNames()) {
          if (location.getName().equals(locationName)) {
            // TODO: put on queue
            StringBuilder messageBuilder = new StringBuilder("New stops @ ");
            messageBuilder.append(location.getName());
            // TODO: cache the location
            final Location resolvedLocation = locationDAO.findByAddress(locationName);

            String trucksAtLocation = FluentIterable.from(locationsToTrucks.get(location.getName()))
                .filter(new Predicate<Truck>() {
                  public boolean apply(Truck input) {
                    String throttleKey = throttleKey(deviceProfile.getDeviceToken(), input.getId(), (long)resolvedLocation.getKey(), today);
                    Object throttle =  memcacheService.get(throttleKey);
                    if (throttle == null) {
                      // TODO: another side effect...fix this
                      memcacheService.put(throttleKey, "hit", Expiration.onDate(today.plusDays(1).toDateTimeAtStartOfDay().toDate()));
                    }
                    return throttle == null;
                  }
                })
                .join(Joiner.on(","));
            if (!Strings.isNullOrEmpty(trucksAtLocation)) {
              messageBuilder.append(": ").append(trucksAtLocation);
              for (NotificationProcessor processor : processors) {
                processor.handle(new PushNotification(messageBuilder.toString(), deviceProfile.getDeviceToken(),
                    deviceProfile.getType()));
              }
            }
            break;
          }
        }
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
