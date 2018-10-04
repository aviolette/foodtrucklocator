package foodtruck.linxup;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.javadocmd.simplelatlng.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LinxupAccountDAO;
import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.OverQueryLimitException;
import foodtruck.model.LinxupAccount;
import foodtruck.model.Location;
import foodtruck.model.Stop;
import foodtruck.model.StopOrigin;
import foodtruck.model.TrackingDevice;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.monitoring.Counter;
import foodtruck.server.security.SecurityChecker;
import foodtruck.time.Clock;
import foodtruck.time.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 8/4/16
 */
class TrackingDeviceServiceImpl implements TrackingDeviceService {

  private static final Logger log = Logger.getLogger(TrackingDeviceServiceImpl.class.getName());
  private final LinxupConnector connector;
  private final TrackingDeviceDAO trackingDeviceDAO;
  private final TruckDAO truckDAO;
  private final GeoLocator locator;
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;
  private final DateTimeFormatter formatter;
  private final SecurityChecker securityChecker;
  private final LinxupAccountDAO linxupAccountDAO;
  private final Provider<Queue> queueProvider;
  private final Provider<TruckStopCache> truckStopCacheProvider;
  private final BlacklistedLocationMatcher blacklistedLocationMatcher;
  private final LocationResolver locationResolver;
  private final Counter counter;

  @Inject
  public TrackingDeviceServiceImpl(TruckStopDAO truckStopDAO, LinxupConnector connector,
      TrackingDeviceDAO trackingDeviceDAO, GeoLocator locator, Clock clock, TruckDAO truckDAO,
      @FriendlyDateTimeFormat DateTimeFormatter formatter, SecurityChecker securityChecker,
      LinxupAccountDAO linxupAccountDAO, Provider<Queue> queueProvider, Provider<TruckStopCache> truckStopCacheProvider,
      BlacklistedLocationMatcher blacklistedLocationMatcher, LocationResolver locationResolver,
      @ErrorCounter Counter errorCounter) {
    this.connector = connector;
    this.truckStopDAO = truckStopDAO;
    this.trackingDeviceDAO = trackingDeviceDAO;
    this.locator = locator;
    this.clock = clock;
    this.truckDAO = truckDAO;
    this.formatter = formatter;
    this.securityChecker = securityChecker;
    this.linxupAccountDAO = linxupAccountDAO;
    this.queueProvider = queueProvider;
    this.truckStopCacheProvider = truckStopCacheProvider;
    this.blacklistedLocationMatcher = blacklistedLocationMatcher;
    this.locationResolver = locationResolver;
    this.counter = errorCounter;
  }

  @Override
  public void synchronize() {
    for (LinxupAccount account : linxupAccountDAO.findActive()) {
      try {
        synchronizeFor(account);
      } catch (IOException io) {
        long count = counter.getCount(account.getTruckId());
        if (count < 2) {
          counter.increment(account.getTruckId());
          log.log(Level.WARNING, io.getMessage() + " " + count, io);
          continue;
        }
        log.log(Level.SEVERE, io.getMessage(), io);
      }
    }
  }

  @Override
  public void synchronizeFor(LinxupAccount account) throws IOException {
    List<Position> positionList = connector.findPositions(account);
    merge(synchronize(positionList, account));
  }

  @Override
  public void enableDevice(Long beaconId, boolean enabled) {
    TrackingDevice device = trackingDeviceDAO.findByIdOpt(beaconId)
        .orElseThrow(() -> new WebApplicationException(4040));
    if (Strings.isNullOrEmpty(device.getTruckOwnerId())) {
      throw new WebApplicationException(404);
    }
    securityChecker.requiresLoggedInAs(device.getTruckOwnerId());
    trackingDeviceDAO.save(TrackingDevice.builder(device)
        .enabled(enabled)
        .build());
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
    TrackingDevice device = trackingDeviceDAO.findByIdOpt(beaconId)
        .orElseThrow(() -> new WebApplicationException(404));
    if (Strings.isNullOrEmpty(device.getTruckOwnerId())) {
      throw new WebApplicationException(404);
    }
    securityChecker.requiresLoggedInAs(device.getTruckOwnerId());
    LinxupAccount account = linxupAccountDAO.findByTruck(device.getTruckOwnerId());
    if (account == null) {
      throw new WebApplicationException(403);
    }
    LinxupMapHistoryResponse response = connector.tripList(account, clock.currentDay()
        .toDateTimeAtStartOfDay(), clock.now(), device.getDeviceNumber());
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
      //noinspection StatementWithEmptyBody
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
    return tripsBuilder.build().stream()
        .map(Trip.Builder::build)
        .collect(Collectors.toList());
  }

  /**
   * Takes the device data the was retrieved and create/update/deletes an existing stops based on the data.
   * @param devices the list of devices retrieved
   */

  private void merge(List<TrackingDevice> devices) {
    TruckStopCache stopCache = truckStopCacheProvider.get();
    for (TrackingDevice device : devices) {
      Truck truck = truckDAO.findByIdOpt(device.getTruckOwnerId()).orElse(null);
      boolean noMorningStops = truck != null && !truck.isMatchesMorningStops() && clock.timeAt(11, 0)
          .isAfter(clock.now());
      if (!device.isEnabled() || !device.isParked() || device.isAtBlacklistedLocation() ||
          device.getLastLocation() == null || noMorningStops) {
        if (noMorningStops) {
          log.log(Level.INFO, "Canceling stops for device {0} since there are no morning stops allowed");
        } else {
          log.log(Level.INFO, "Canceling stops for device: {0}", device);
        }
        //noinspection ConstantConditions
        cancelAnyStops(device, stopCache.get(device.getTruckOwnerId()));
        continue;
      }
      mergeTruck(device, stopCache);
    }
  }

  private void cancelAnyStops(TrackingDevice device, List<TruckStop> activeStops) {
    //noinspection ConstantConditions
    for (TruckStop stop : activeStops) {
      if (stop.activeDuring(clock.now()) && device.getId()
          .equals(stop.getCreatedWithDeviceId())) {
        long stopId = truckStopDAO.save(TruckStop.builder(stop)
            .endTime(clock.now())
            .appendNote(
                device.isEnabled() ? "Ended stop since because truck is moving" : "Ended stop since beacon was disabled")
            .build());
        if (device.isEnabled()) {
          Queue queue = queueProvider.get();
          queue.add(TaskOptions.Builder.withUrl("/cron/notify_stop_ended")
              .param("stopId", String.valueOf(stopId))
              .param("deviceId", String.valueOf(device.getKey())));
        }
        return;
      }
    }
  }

  private TruckStop.Builder stop(Truck truck, TrackingDevice device) {
    DateTime now = clock.now();
    return TruckStop.builder()
        .appendNote("Created by beacon on " + formatter.print(now))
        .lastUpdated(clock.now())
        .startTime(device.getLastBroadcast())
        .endTime(now.plusHours(2))
        .createdWithDeviceId(device.getId())
        .fromBeacon(device.getLastBroadcast())
        .location(device.getLastLocation())
        .origin(StopOrigin.LINXUP)
        .truck(truck);
  }

  private void mergeTruck(TrackingDevice device, TruckStopCache stopCache) {
    Matches matches = matchTruck(stopCache, device);
    DateTime now = clock.now();
    TruckStop stop;
    Optional<TruckStop> currentHolder = matches.getCurrent();
    if (currentHolder.isPresent()) {
      TruckStop current = currentHolder.get();
      if (current.getLocation()
          .sameName(device.getLastLocation()) || current.getLocation()
          .withinToleranceOf(device.getLastLocation())) {
        if (current.getEndTime()
            .isBefore(now.plusMinutes(15))) {
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
      } else if (current.getManuallyUpdated() != null && device.getLastBroadcast() != null &&
          current.getManuallyUpdated()
              .isAfter(device.getLastBroadcast())) {
        log.log(Level.WARNING, "Stop was manually edited {0}.  Ignoring until new broadcast.", current);
        // this stop was manually edited so do thing.
        return;
      } else {
        truckStopDAO.save(current.withEndTime(device.getLastBroadcast()));
        stop = stop(matches.getTruck(), device).build();
      }
    } else {
      DateTime endTime;
      Optional<TruckStop> afterHolder = matches.getAfter();
      if (afterHolder.isPresent()) {
        TruckStop after = afterHolder.get();
        endTime = after.getStartTime()
            .isBefore(now.plusHours(2)) ? after.getStartTime()
            .minusMinutes(5) : now.plusHours(2);
      } else {
        endTime = now.plusHours(2);
      }
      stop = stop(matches.getTruck(), device).endTime(endTime)
          .build();
    }
    long stopId = truckStopDAO.save(stop);
    if (stop.isNew()) {
      Queue queue = queueProvider.get();
      queue.add(TaskOptions.Builder.withUrl("/cron/notify_stop_created")
          .param("stopId", String.valueOf(stopId))
          .param("deviceId", String.valueOf(device.getKey())));
    }
  }

  private Matches matchTruck(TruckStopCache stopCache, TrackingDevice device) {
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
        if (device.getKey()
            .equals(stop.getCreatedWithDeviceId()) || (stop.getCreatedWithDeviceId() == null && stop.getLocation()
            .sameName(device.getLastLocation()))) {
          currentStop = stop;
        }
      } else if (afterStop == null && stop.getStartTime()
          .isAfter(now)) {
        afterStop = stop;
      }
    }
    return new Matches(aCurrentStop, currentStop, afterStop,
        truck == null ? truckDAO.findByIdOpt(device.getTruckOwnerId()).orElse(null) : truck);
  }

  /**
   * Synchronize what is returned with existing tracking devices in the DB.  If there is new information, update it in
   * DB.
   */
  private List<TrackingDevice> synchronize(List<Position> positions, LinxupAccount linxupAccount) {
    // wish I had streams here
    Map<String, TrackingDevice> deviceMap = Maps.newHashMap();
    for (TrackingDevice device : trackingDeviceDAO.findAll()) {
      deviceMap.put(device.getDeviceNumber(), device);
    }
    ImmutableList.Builder<TrackingDevice> devices = ImmutableList.builder();
    //noinspection NullableProblems
    for (Position position : positions) {
      TrackingDevice device = deviceMap.get(position.getDeviceNumber());
      TrackingDevice.Builder builder = TrackingDevice.builder(device);
      log.log(Level.INFO, "Synchronizing: {0} {1}", new Object[] {position, device});
      Location location = locationResolver.resolve(position, device, linxupAccount);
      // TODO: it would be nice if the actual device could calculate this, but for now we look to see if it changed.
      boolean parked = position.getSpeedMph() == 0 && (device == null || device.getLastLocation() == null ||
          device.getLastLocation()
              .getName()
              .equals(location.getName()) || device.getLastLocation()
          .within(0.05)
          .milesOf(location));
      if (parked) {
        try {
          location = MoreObjects.firstNonNull(locator.reverseLookup(location), location);
        } catch (OverQueryLimitException oqe) {
          log.log(Level.WARNING, "Over query limit", oqe);
        }
      } else {
        log.log(Level.INFO, "Did not lookup {0} because we are not parked", location);
      }
      // Trying to tame the blips where the GPS goes all over the place when its still parked.
      if (device != null && device.getLastLocation() != null && location != null && device.getLastLocation()
          .getName()
          .equals(location.getAlias())) {
        log.info("Detected blip");
        location = device.getLastLocation();
      }
      log.log(Level.INFO, "Device State: {0}\n {1}\n {2}\n{3}", new Object[]{position, device, location, parked});
      try {
        location = locator.reverseLookup(location);
      } catch (OverQueryLimitException oqe) {
        log.log(Level.WARNING, "Over query limit", oqe);
      }
      builder.deviceNumber(position.getDeviceNumber())
          .lastLocation(location)
          .parked(parked)
          .lastActualLocation(position.toLocation())
          .degreesFromNorth(position.getDirection())
          .fuelLevel(position.getFuelLevel())
          .batteryCharge(position.getBatteryCharge())
          .truckOwnerId(linxupAccount.getTruckId())
          .atBlacklistedLocation(blacklistedLocationMatcher.isBlacklisted(device))
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
      return Optional.ofNullable(current);
    }

    public Optional<TruckStop> getAfter() {
      return Optional.ofNullable(after);
    }
  }
}
