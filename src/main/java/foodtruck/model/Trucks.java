package foodtruck.model;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * A database of trucks that can be retrieved by their id.
 * @author aviolette@gmail.com
 * @since 9/22/11
 */
public class Trucks  {
  private final Map<String, Truck> trucks;
  public Trucks(Map<String, Truck> trucks) {
    this.trucks = trucks;
  }

  public @Nullable Truck findById(String truckId) {
    return trucks.get(truckId);
  }

  public Collection<Truck> allTrucks() {
    return trucks.values();
  }
}
