package foodtruck.dao;

import java.util.List;

import foodtruck.model.TrackingDevice;

/**
 * @author aviolette
 * @since 7/28/16
 */
public interface TrackingDeviceDAO extends DAO<Long, TrackingDevice> {
  /**
   * Find all beacons owned by a truck.
   */
  List<TrackingDevice> findByTruckId(String truckId);
}
