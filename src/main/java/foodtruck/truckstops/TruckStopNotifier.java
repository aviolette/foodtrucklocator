package foodtruck.truckstops;

import foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since 5/24/12
 */
public interface TruckStopNotifier {
  public void added(TruckStop stop);

  public void removed(TruckStop stop);

  public void terminated(TruckStop stop);
}
