package foodtruck.beaconnaise;

import foodtruck.model.TruckStop;

/**
 * @author aviolette
 * @since 7/29/13
 */
public class BeaconResponse {
  private final TruckStop stop;
  public BeaconResponse(TruckStop stop) {
    this.stop = stop;
  }

  public TruckStop getStop() {
    return this.stop;
  }
}
