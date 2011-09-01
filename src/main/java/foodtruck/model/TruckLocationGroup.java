package foodtruck.model;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Represents a grouping of food trucks at a location.
 * @author aviolette@gmail.com
 * @since 9/1/11
 */
public class TruckLocationGroup {
  private @Nullable final Location location;
  private final Collection<Truck> trucks;

  /**
   * Constructs a truck location group.
   * @param location the location (a null location means 'unknown')
   * @param trucks   the trucks at that location
   */
  public TruckLocationGroup(@Nullable Location location, Collection<Truck> trucks) {
    this.location = location;
    this.trucks = trucks;
  }

  public Location getLocation() {
    return location;
  }

  public Collection<Truck> getTrucks() {
    return trucks;
  }

}
