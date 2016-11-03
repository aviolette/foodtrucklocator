package foodtruck.linxup;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.javadocmd.simplelatlng.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LinxupAccountDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.model.LinxupAccount;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.TrackingDevice;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.notifications.NotificationService;
import foodtruck.server.security.SecurityChecker;
import foodtruck.util.Clock;
import foodtruck.util.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 8/4/16
 */
class TruckMonitorServiceImpl implements TruckMonitorService {
  private static final Logger log = Logger.getLogger(TruckMonitorServiceImpl.class.getName());
  private final LinxupConnector connector;
  private final TrackingDeviceDAO trackingDeviceDAO;
  private final LocationDAO locationDAO;
  private final TruckDAO truckDAO;
  private final GeoLocator locator;
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;
  private final DateTimeFormatter formatter;
  private final SecurityChecker securityChecker;
  private final NotificationService notificationService;
  private final LinxupAccountDAO linxupAccountDAO;

  @Inject
  public TruckMonitorServiceImpl(TruckStopDAO truckStopDAO, LinxupConnector connector,
      TrackingDeviceDAO trackingDeviceDAO, GeoLocator locator, Clock clock, TruckDAO truckDAO,
      @FriendlyDateTimeFormat DateTimeFormatter formatter, LocationDAO locationDAO, SecurityChecker securityChecker,
      NotificationService notificationService, LinxupAccountDAO linxupAccountDAO) {
    this.connector = connector;
    this.truckStopDAO = truckStopDAO;
    this.trackingDeviceDAO = trackingDeviceDAO;
    this.locator = locator;
    this.clock = clock;
    this.truckDAO = truckDAO;
    this.formatter = formatter;
    this.locationDAO = locationDAO;
    this.securityChecker = securityChecker;
    this.notificationService = notificationService;
    this.linxupAccountDAO = linxupAccountDAO;
  }

  @Override
  public void synchronize() {
    for (LinxupAccount account : linxupAccountDAO.findActive()) {
      synchronizeFor(account);
    }
  }

  @Override
  public void synchronizeFor(LinxupAccount account) {
    List<Position> positionList = connector.findPositions(account);
    try {
      merge(synchronize(positionList, account.getTruckId()));
    } catch (ExecutionException e) {
      throw Throwables.propagate(e);
    }
  }


  @Override
  public void enableDevice(Long beaconId, boolean enabled) {
    TrackingDevice device = trackingDeviceDAO.findById(beaconId);
    if (device == null || Strings.isNullOrEmpty(device.getTruckOwnerId())) {
      throw new WebApplicationException(404);
    }
    securityChecker.requiresLoggedInAs(device.getTruckOwnerId());
    trackingDeviceDAO.save(TrackingDevice.builder(device).enabled(enabled).build());
    if (enabled) {
      // TODO: probably not what we want to do here.
      synchronize();
    } else if (!Strings.isNullOrEmpty(device.getTruckOwnerId())) {
      cancelAnyStops(device, truckStopDAO.findDuring(device.getTruckOwnerId(), clock.currentDay()));
    }
  }

  @Override
  public void removeDevicesFor(String truckId) {
    for (TrackingDevice device : trackingDeviceDAO.findByTruckId(truckId)) {
      trackingDeviceDAO.delete(device.getId());
    }
  }

  @Override
  public List<Trip> getRecentTripList(Long beaconId) {
    TrackingDevice device = trackingDeviceDAO.findById(beaconId);
    if (device == null || Strings.isNullOrEmpty(device.getTruckOwnerId())) {
      throw new WebApplicationException(404);
    }
    securityChecker.requiresLoggedInAs(device.getTruckOwnerId());
    LinxupAccount account = linxupAccountDAO.findByTruck(device.getTruckOwnerId());
    if (account == null) {
      throw new WebApplicationException(403);
    }
    LinxupMapHistoryResponse response = connector.tripList(account, clock.now()
        .minusDays(1), clock.now(), device.getDeviceNumber());
    ImmutableList.Builder<Trip.Builder> tripsBuilder = ImmutableList.builder();
    Stop first = null;
    for (Stop stop : response.getStops()) {
      if ("Idling".equals(stop.getStopType())) {
        continue;
      }
      if (first == null) {
        first = stop;
      } else {
        tripsBuilder.add(Trip.builder()
            .start(first.getLocation())
            .startTime(first.getEndTime())
            .end(stop.getLocation())
            .addPosition(Position.builder()
                .latLng(new LatLng(first.getLocation()
                    .getLatitude(), first.getLocation()
                    .getLongitude()))
                .build())
            .endTime(stop.getBeginTime()));
        first = stop;
      }
    }
    List<Trip.Builder> tripResponse = tripsBuilder.build();
    Iterator<Trip.Builder> tripit = tripResponse.iterator();
    Trip.Builder trip = null;
    for (Position position : response.getPositions()) {
      if (trip == null) {
        if (tripit.hasNext()) {
          trip = tripit.next();
        } else {
          break;
        }
      }
      if (position.getDate()
          .isBefore(trip.getStartTime())) {
        // what to do?
      } else if (position.getDate()
          .isAfter(trip.getEndTime())) {
        while (tripit.hasNext()) {
          trip = tripit.next();
          if (position.getDate()
              .isAfter(trip.getStartTime()
                  .minusSeconds(1)) && position.getDate()
              .isBefore(trip.getEndTime())) {
            trip.addPosition(position);
            break;
          } else if (position.getDate()
              .isBefore(trip.getStartTime())) {
            break;
          }
        }
      } else {
        trip.addPosition(position);
      }
    }
    return FluentIterable.from(tripsBuilder.build())
        .transform(new Function<Trip.Builder, Trip>() {
          public Trip apply(Trip.Builder input) {
            return input.build();
          }
        })
        .toList();
  }

  /**
   * Takes the device data the was retrieved and create/update/deletes an existing stops based on the data.
   * @param devices the list of devices retrieved
   */

  private void merge(List<TrackingDevice> devices) throws ExecutionException {
    LoadingCache<String, List<TruckStop>> stopCache = CacheBuilder.newBuilder()
        .build(new CacheLoader<String, List<TruckStop>>() {
          @SuppressWarnings("NullableProblems")
          public List<TruckStop> load(String truckId) throws Exception {
            return truckStopDAO.findDuring(truckId, clock.currentDay());
          }
        });
    for (TrackingDevice device : devices) {
      if (!device.isEnabled() || !device.isParked() || device.isAtBlacklistedLocation() ||
          device.getLastLocation() == null) {
        log.log(Level.INFO, "Canceling stops for device: {0}", device);
        //noinspection ConstantConditions
        cancelAnyStops(device, stopCache.get(device.getTruckOwnerId()));
        continue;
      }
      mergeTruck(device, stopCache);
    }
  }

  private boolean atBlacklistedLocation(@Nullable String ownerId, @Nullable Location lastLocation,
      LoadingCache<String, List<Location>> blacklistCache) throws ExecutionException {
    if (Strings.isNullOrEmpty(ownerId)) {
      return false;
    }
    List<Location> locations = blacklistCache.get(ownerId);
    if (locations == null || lastLocation == null) {
      return false;
    }
    for (Location location : locations) {
      if (location.within(location.getRadius()).milesOf(lastLocation)) {
        return true;
      }
    }
    return false;
  }

  private void cancelAnyStops(TrackingDevice device, List<TruckStop> activeStops) {
    //noinspection ConstantConditions
    for (TruckStop stop : activeStops) {
      if (stop.activeDuring(clock.now()) && device.getId().equals(stop.getCreatedWithDeviceId())) {
        truckStopDAO.save(TruckStop.builder(stop)
            .endTime(clock.now())
            .appendNote(device.isEnabled() ? "Ended stop since because truck is moving" :
                "Ended stop since beacon was disabled")
            .build());
        return;
      }
    }
  }

  private TruckStop.Builder stop(Truck truck, TrackingDevice device) {
    DateTime now = clock.now();
    return TruckStop.builder()
        .appendNote("Created by beacon on " + formatter.print(now))
        .lastUpdated(clock.now())
        .startTime(device.getLastBroadcast()).endTime(now.plusHours(2)).createdWithDeviceId(device.getId())
        .fromBeacon(device.getLastBroadcast())
        .location(device.getLastLocation())
        .origin(StopOrigin.LINXUP)
        .truck(truck);
  }

  private void mergeTruck(TrackingDevice device,
      LoadingCache<String, List<TruckStop>> stopCache) throws ExecutionException {
    Matches matches = matchTruck(stopCache, device);
    DateTime now = clock.now();
    TruckStop stop;
    Optional<TruckStop> currentHolder = matches.getCurrent();
    if (currentHolder.isPresent()) {
      TruckStop current = currentHolder.get();
      if (current.getLocation()
          .sameName(device.getLastLocation()) || current.getLocation()
          .within(0.05)
          .milesOf(device.getLastLocation())) {
        if (current.getEndTime().isBefore(now.plusMinutes(15))) {
          TruckStop.Builder builder = TruckStop.builder(current)
              .appendNote("Extended time by 60 minutes at " + formatter.print(now))
              .endTime(current.getEndTime()
                  .plusMinutes(60))
              .fromBeacon(device.getLastBroadcast());
          stop = builder.build();
        } else {
          // no need to update anything
          return;
        }
      } else {
        truckStopDAO.save(current.withEndTime(device.getLastBroadcast()));
        stop = stop(matches.getTruck(), device).build();
      }
    } else {
      DateTime endTime;
      Optional<TruckStop> afterHolder = matches.getAfter();
      if (afterHolder.isPresent()) {
        TruckStop after = afterHolder.get();
        endTime = after.getStartTime().isBefore(now.plusHours(2)) ? after.getStartTime().minusMinutes(5) : now.plusHours(2);
      } else {
        endTime = now.plusHours(2);
      }
      stop = stop(matches.getTruck(), device).endTime(endTime).build();
    }
    truckStopDAO.save(stop);
    if (stop.isNew()) {
      notificationService.notifyStopStart(stop);
    }
  }

  private Matches matchTruck(LoadingCache<String, List<TruckStop>> stopCache,
      TrackingDevice device) throws ExecutionException {
    List<TruckStop> truckStops;
    //noinspection ConstantConditions
    truckStops = stopCache.get(device.getTruckOwnerId());

    Truck truck = null;
    TruckStop currentStop = null, aCurrentStop = null, afterStop = null;
    DateTime now = clock.now();
    for (TruckStop stop : truckStops) {
      truck = stop.getTruck();
      if (stop.activeDuring(now)) {
        if (stop.getCreatedWithDeviceId() == null) {
          aCurrentStop = stop;
        }
        if (device.getKey().equals(stop.getCreatedWithDeviceId()) || (stop.getCreatedWithDeviceId() == null &&
            stop.getLocation().sameName(device.getLastLocation()))) {
          currentStop = stop;
        }
      } else if (afterStop == null && stop.getStartTime().isAfter(now)) {
        afterStop = stop;
      }
    }
    return new Matches(aCurrentStop, currentStop, afterStop,
        truck == null ? truckDAO.findById(device.getTruckOwnerId()) : truck);
  }

  /**
   * Synchronize what is returned with existing tracking devices in the DB.  If there is new information, update it in
   * DB.
   */
  private List<TrackingDevice> synchronize(List<Position> positions, String truckId) throws ExecutionException {
    // wish I had streams here
    Map<String, TrackingDevice> deviceMap = Maps.newHashMap();
    for (TrackingDevice device : trackingDeviceDAO.findAll()) {
      deviceMap.put(device.getDeviceNumber(), device);
    }
    ImmutableList.Builder<TrackingDevice> devices = ImmutableList.builder();
    //noinspection NullableProblems
    LoadingCache<String, List<Location>> blacklistCache = CacheBuilder.newBuilder()
        .build(new CacheLoader<String, List<Location>>() {
          public List<Location> load(String key) throws Exception {
            Truck truck = truckDAO.findById(key);
            //noinspection ConstantConditions,ConstantConditions
            return FluentIterable.from(truck.getBlacklistLocationNames())
                .transform(new Function<String, Location>() {
                  public Location apply(String name) {
                    return locationDAO.findByAddress(name);
                  }
                })
                .filter(Predicates.<Location>notNull())
                .toList();
          }
        });
    for (Position position : positions) {
      TrackingDevice device = deviceMap.get(position.getDeviceNumber());
      TrackingDevice.Builder builder = TrackingDevice.builder(device);
      Location location = Location.builder().lat(position.getLatLng().getLatitude())
          .lng(position.getLatLng().getLongitude())
          .name("UNKNOWN")
          .build();
      // TODO: it would be nice if the actual device could calculate this, but for now we look to see if it changed.
      boolean parked = position.getSpeedMph() == 0 && (device == null || device.getLastLocation() == null || device.getLastLocation()
          .getName()
          .equals(location.getName()) || device.getLastLocation()
          .within(0.05)
          .milesOf(location));
      boolean atBlacklisted = false;
      if (device != null) {
        atBlacklisted = atBlacklistedLocation(device.getTruckOwnerId(), device.getLastLocation(), blacklistCache);
      }
      log.log(Level.INFO, "Device State: {0}\n {1}\n {2}", new Object[]{position, device, location});
      builder.deviceNumber(position.getDeviceNumber())
          .lastLocation(locator.reverseLookup(location))
          .parked(parked)
          .degreesFromNorth(position.getDirection())
          .fuelLevel(position.getFuelLevel())
          .batteryCharge(position.getBatteryCharge())
          .truckOwnerId(truckId)
          .atBlacklistedLocation(atBlacklisted)
          .lastBroadcast(position.getDate())
          .label(position.getVehicleLabel());
      TrackingDevice theDevice = builder.build();
      trackingDeviceDAO.save(theDevice);
      devices.add(theDevice);
    }
    return devices.build();
  }

  private static class Matches {
    private final TruckStop current, after;
    private final Truck truck;

    Matches(TruckStop current, TruckStop exact, TruckStop after, Truck truck) {
      this.current = exact == null ? current : exact;
      this.after = after;
      this.truck = truck;
    }

    public Truck getTruck() {
      return truck;
    }

    public Optional<TruckStop> getCurrent() {
      return Optional.fromNullable(current);
    }

    public Optional<TruckStop> getAfter() {
      return Optional.fromNullable(after);
    }
  }
}
