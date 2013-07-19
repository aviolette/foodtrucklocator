package foodtruck.beaconnaise;

import com.google.common.base.Objects;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 7/20/13
 */
public class BeaconSignal {
  private String truckId;
  private Location location;

  public BeaconSignal(String truckId, Location location) {
    this.truckId = truckId;
    this.location = location;
  }

  public String getTruckId() {
    return truckId;
  }

  public Location getLocation() {
    return location;
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("truckId", truckId)
        .add("location", location)
        .toString();
  }
}
