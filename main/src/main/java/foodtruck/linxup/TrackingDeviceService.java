package foodtruck.linxup;

import java.io.IOException;
import java.util.List;

import foodtruck.model.LinxupAccount;

/**
 * @author aviolette
 * @since 8/4/16
 */
public interface TrackingDeviceService {
  /**
   * Connects with upstream position provider and creates/deletes/modifies existing stops with data that is retrieved.
   */
  void synchronize();

  /**
   * Synchronizes just one linxup account
   */
  void synchronizeFor(LinxupAccount account) throws IOException;

  /**
   * Enables or disables the beacon.  If a beacon is disabled, existing any active stops created by the beacon will be
   * stopped.  If a beacon is enabled, a synchronization will be attempted for that device.
   */
  void enableDevice(Long deviceId, boolean enabled);

  /**
   * Removes a truck's tracking devices
   */
  void removeDevicesFor(String truckId);

  /**
   * Returns a truck's recent trip activity.
   * @param beaconId the beacon id
   */
  List<Trip> getRecentTripList(Long beaconId);
}
