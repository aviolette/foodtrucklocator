package foodtruck.linxup;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import foodtruck.caching.Cacher;
import foodtruck.mail.SystemNotificationService;
import foodtruck.model.LinxupAccount;
import foodtruck.model.Location;
import foodtruck.model.Stop;
import foodtruck.model.TrackingDevice;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 11/29/16
 */
class LocationResolverImpl implements LocationResolver {
  private static final Logger log = Logger.getLogger(LocationResolverImpl.class.getName());
  private final Cacher cache;
  private final Clock clock;
  private final LinxupConnector connector;
  private final SystemNotificationService systemNotificationService;

  @Inject
  public LocationResolverImpl(Clock clock, LinxupConnector connector,
      SystemNotificationService systemNotificationService, Cacher cacher) {
    this.cache = cacher;
    this.clock = clock;
    this.connector = connector;
    this.systemNotificationService = systemNotificationService;
  }

  @Override
  public Location resolve(Position position, @Nullable TrackingDevice previousDeviceRecording,
      LinxupAccount linxupAccount) {
    Location currentlyRecordPosition = position.toLocation();
    // device will be null when this is the first time we've seen a particular device
    if (previousDeviceRecording == null || position.getSpeedMph() > 0) {
      return currentlyRecordPosition;
    }
    // In the case where the CURRENT and LAST position have not varied by much, return the last position
    Location lastRecordedPosition = previousDeviceRecording.getPreciseLocation();
    if (lastRecordedPosition == null || currentlyRecordPosition.within(0.01)
        .milesOf(lastRecordedPosition)) {
      return lastRecordedPosition;
    }
    log.log(Level.INFO, "Sanity check: position-parked: {0} device-parked: {1} distance from last position {2}",
        new Object[]{position.isParked(), previousDeviceRecording.isParked(),
            currentlyRecordPosition.distanceFrom(previousDeviceRecording.getLastLocation())});
    // the case where there was no intermediate movement, means there may be an anomaly
    if (position.isParked() && previousDeviceRecording.isParked() && currentlyRecordPosition.within(0.50)
        .milesOf(previousDeviceRecording.getLastLocation())) {
      log.log(Level.INFO, "Checking for anomaly {0}\n\n {1}",
          new Object[]{currentlyRecordPosition, previousDeviceRecording});
      if (detectAnomaly(previousDeviceRecording, linxupAccount, currentlyRecordPosition)) {
        log.log(Level.INFO, "Returning {0}", previousDeviceRecording.getLastLocation());
        return previousDeviceRecording.getLastLocation();
      }
    }
    return currentlyRecordPosition;
  }

  /**
   * Search through the history and compare last history location with the current location.  If the current location is
   * not within tolerance of stop, then return true. If there is no anomaly, return false.
   */
  private boolean detectAnomaly(TrackingDevice device, LinxupAccount linxupAccount, Location currentLocation) {
    String key = "anomaly-detected-for-" + device.getDeviceNumber();
    if (cache.contains(key)) {
      log.log(Level.INFO, "Anomaly detected for device {0} but pulled from memcache {1}", new Object[]{device, key});
      return true;
    }

    try {
      LinxupMapHistoryResponse response = connector.tripList(linxupAccount, clock.timeAt(0, 0), clock.timeAt(23, 59),
          device.getDeviceNumber());
      Stop stop = response.lastStopFor(device.getDeviceNumber());
      // if the current location is within tolerance of the last location or if the last location that was recorded
      // was actually recorded a long time ago.
      if (stop != null && (!stop.getLocation()
          .withinToleranceOf(currentLocation) || stop.getEndTime()
          .isBefore(clock.now()
              .minusMinutes(30)))) {
        if ("true".equals(System.getProperty("foodtrucklocator.location.anomaly.detection"))) {
          systemNotificationService.notifyDeviceAnomalyDetected(stop, device);
        }
        log.log(Level.WARNING, "Anomaly detected {0} {1}", new Object[]{stop.getLocation(), currentLocation});
        // only perform anomaly detection once per-broadcast (the device usually broadcasts every hour when parked)
        cache.put(key, true, device.getLastBroadcast()
            .plusMinutes(59));
        return true;
      } else {
        log.log(Level.INFO, "Anomaly not detected\n\n {0}\n\n {1}\n\n", new Object[]{stop, currentLocation});
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return false;
  }

}
