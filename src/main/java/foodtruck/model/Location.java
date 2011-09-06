package foodtruck.model;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.common.base.Objects;

/**
 * Latitude and Longitude.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
@XmlRootElement
public class Location {
  private double lng;
  private double lat;
  private String name;

  // for jaxb
  public Location() {
  }

  public Location(double lat, double lng, @Nullable String name) {
    this.lat = lat;
    this.lng = lng;
    this.name = name;
  }

  public Location(double lat, double lng) {
    this(lat, lng, null);
  }

  @XmlElement
  public double getLatitude() {
    return lat;
  }

  @XmlElement
  public double getLongitude() {
    return lng;
  }

  @XmlElement
  public String getName() {
    return this.name;
  }

  @XmlTransient
  public boolean isNamed() {
    return !Strings.isNullOrEmpty(this.name);
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
}
