package foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * Latitude and Longitude.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class Location extends ModelEntity {
  private final double lng;
  private final double lat;
  private final String name;
  private final boolean valid;
  private final @Nullable String description;
  private final @Nullable String url;
  private final boolean eventSpecific;
  private final double radius;

  public Location(Builder builder) {
    super(builder.key);
    lng = builder.lng;
    lat = builder.lat;
    name = builder.name;
    valid = builder.valid;
    description = builder.description;
    url = builder.url;
    eventSpecific = builder.eventSpecific;
    radius = builder.radius;
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

  public boolean isValid() {
    return this.valid;
  }

  public boolean isEvent() {
    return eventSpecific;
  }

  public @Nullable String getDescription() {
    return this.description;
  }

  public @Nullable String getUrl() {
    return url;
  }

  // TODO: this probably should be refactored out of here

  /**
   * Return true if the location has been properly resolved.
   */
  public boolean isResolved() {
    return valid && lat != 0 && lng != 0;
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

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Location loc) {
    return new Builder(loc);
  }

  public Location withKey(Object key) {
    return builder(this).key(key).build();
  }

  public double getRadius() {
    return radius;
  }

  public static class Builder {
    private Object key;
    private double lat;
    private double lng;
    private String name;
    private boolean valid = true;
    private @Nullable String description;
    private boolean eventSpecific;
    private @Nullable String url;
    public double radius = 0d;

    public Builder(Location location) {
      key = location.getKey();
      lat = location.getLatitude();
      lng = location.getLongitude();
      name = location.getName();
      valid = location.isValid();
    }

    public Builder() {
    }

    public Builder radius(double radius) {
      this.radius = radius;
      return this;
    }

    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    public Builder eventSpecific(boolean eventSpecific) {
      this.eventSpecific = eventSpecific;
      return this;
    }

    public Builder url(@Nullable String url) {
      this.url = url;
      return this;
    }

    public Builder key(Object key) {
      this.key = key;
      return this;
    }

    public Builder lat(double latitude) {
      this.lat = latitude;
      return this;
    }

    public Builder lng(double longitude) {
      this.lng = longitude;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder valid(boolean valid) {
      this.valid = valid;
      return this;
    }

    public Location build() {
      return new Location(this);
    }
  }
}
