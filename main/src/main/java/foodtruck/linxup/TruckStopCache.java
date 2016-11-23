package foodtruck.linxup;

import java.util.List;

import foodtruck.model.TruckStop;

/**
 * @author aviolette
 * @since 11/22/16
 */
interface TruckStopCache {
  List<TruckStop> get(String truckId);
}
