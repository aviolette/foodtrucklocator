package foodtruck.model;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.appengine.repackaged.com.google.common.base.Predicate;
import com.google.appengine.repackaged.com.google.common.collect.Iterables;

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

  /**
   * Returns all the trucks
   */
  public Collection<Truck> allTrucks() {
    return trucks.values();
  }

  /**
   * Returns all the trucks that support twitter location resolution
   */
  public Iterable<Truck> allTwitterTrucks() {
    return Iterables.filter(allTrucks(), new Predicate<Truck>() {
      public boolean apply(Truck truck) {
        return truck.isUsingTwittalyzer();
      }
    });
  }

  public Truck findByTwitterId(String screenName) {
    // TODO: wrong
    return trucks.get(screenName);
  }
}
