package foodtruck.model;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a grouping of food trucks at a location.
 * @author aviolette@gmail.com
 * @since 9/1/11
 */
@XmlRootElement
public class TruckLocationGroup {
  private @Nullable Location location;
  private Collection<Truck> trucks;

  // for JAXB
  public TruckLocationGroup() {
  }

  /**
   * Constructs a truck location group.
   * @param location the location (a null location means 'unknown')
   * @param trucks   the trucks at that location
   */
  public TruckLocationGroup(@Nullable Location location, Collection<Truck> trucks) {
    this.location = location;
    this.trucks = trucks;
  }

  @XmlElement
  public Location getLocation() {
    return location;
  }

  @XmlElement
  public Collection<Truck> getTrucks() {
    return trucks;
  }
}
