package foodtruck.linxup;

import foodtruck.model.LinxupAccount;

/**
 * @author aviolette
 * @since 8/4/16
 */
public interface TruckMonitorService {
  /**
   * Connects with upstream position provider and creates/deletes/modifies existing
   * stops with data that is retrieved.
   */
  void synchronize();

  void synchronizeFor(LinxupAccount account);

  /**
   * Enables or disables the beacon.  If a beacon is disabled, existing any active stops
   * created by the beacon will be stopped.  If a beacon is enabled, a synchronization will be attempted for that
   * device.
   */
  void enableDevice(Long deviceId, boolean enabled);

  void removeDevicesFor(String truckId);
}
