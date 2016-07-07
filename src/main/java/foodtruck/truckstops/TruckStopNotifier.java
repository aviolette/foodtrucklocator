package foodtruck.truckstops;

import foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since 5/24/12
 */
public interface TruckStopNotifier {
  void added(TruckStop stop);

  void removed(TruckStop stop);

  void terminated(TruckStop stop);
}
