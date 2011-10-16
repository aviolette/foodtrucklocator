package foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * Latitude and Longitude.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class Location {
  private final double lng;
  private final double lat;
  private final String name;

  public Location(double lat, double lng, @Nullable String name) {
    this.lat = lat;
    this.lng = lng;
    this.name = name;
  }

  public Location(double lat, double lng) {
    this(lat, lng, null);
  }

  public double getLatitude() {
    return lat;
  }

  public double getLongitude() {
    return lng;
  }

  public String getName() {
    return this.name;
  }

  // TODO: this probably should be refactored out of here
  /**
   * Return true if the location has been properly resolved.
   */
  public boolean isResolved() {
    return lat != 0 && lng != 0;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("Latitude", lat)
        .add("Longitude", lng)
        .add("Name", name)
        .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(lng, lat);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null || !(o instanceof Location)) {
      return false;
    }
    Location obj = (Location) o;
    return lat == obj.lat && lng == obj.lng;
  }

  /**
   * Returns a new Location with an updated name
   */
  public Location withName(String name) {
    return new Location(lat, lng, name);
  }
}
