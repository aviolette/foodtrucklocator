package foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;


/**
 * Latitude and Longitude.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class Location extends ModelEntity {
  public static final Function<Location, String> TO_NAME = new Function<Location, String> () {
    @Override public String apply(@Nullable Location input) {
      return input.getName();
    }
  };
  private LatLng latLng;
  private final String name;
  private final boolean valid;
  private final @Nullable String description;
  private final @Nullable String url;
  private final boolean eventSpecific;
  private final double radius;
  private final boolean popular;
  private final boolean justResolved;
  private final boolean autocomplete;
  private final @Nullable String alias;

  public Location(Builder builder) {
    super(builder.key);
    latLng = new LatLng(builder.lat, builder.lng);
    name = builder.name;
    valid = builder.valid;
    description = builder.description;
    url = builder.url;
    eventSpecific = builder.eventSpecific;
    radius = builder.radius;
    popular = builder.popular;
    justResolved = builder.justResolved;
    autocomplete = builder.autocomplete;
    alias = builder.alias;
  }

  public boolean isPopular() {
    return popular;
  }

  public double getLatitude() {
    return latLng.getLatitude();
  }

  public double getLongitude() {
    return latLng.getLongitude();
  }

  public String getName() {
    return this.name;
  }

  public Location wasJustResolved() {
    return Location.builder(this).wasJustResolved(true).build();
  }

  public boolean isValid() {
    return this.valid;
  }

  public boolean isJustResolved() {
    return justResolved;
  }

  public boolean isAutocomplete() {
    return autocomplete;
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
    return valid && latLng.getLatitude() != 0 && latLng.getLongitude() != 0;
  }

  public boolean containedWithRadiusOf(Location loc) {
    if (equals(loc)) {
      return true;
    }
    if (loc.getRadius() == 0) {
      return false;
    }
    return within(loc.getRadius()).milesOf(loc);
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("Lat/Lng", latLng)
        .add("Name", name)
        // appengine throws Method undefined errors 'cause of this...not sure why
        .add("Radius", String.valueOf(radius))
        .toString();
  }

  @Override
  public int hashCode() {
    return latLng.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null || !(o instanceof Location)) {
      return false;
    }
    Location obj = (Location) o;
    return obj.latLng.equals(latLng);
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

  public double distanceFrom(Location mapCenter) {
    return LatLngTool.distance(latLng, mapCenter.latLng, LengthUnit.MILE);
  }

  public ScalarDistanceRequest within(double distance) {
    return new ScalarDistanceRequest(distance);
  }

  public Location withName(String name) {
    return Location.builder(this).name(name).build();
  }

  @Nullable public String getAlias() {
    return alias;
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
    private boolean popular;
    private boolean justResolved;
    private boolean autocomplete;
    private @Nullable String alias;

    public Builder(Location location) {
      key = location.getKey();
      lat = location.getLatitude();
      lng = location.getLongitude();
      name = location.getName();
      valid = location.isValid();
      description = location.getDescription();
      eventSpecific = location.isEvent();
      url = location.getUrl();
      radius = location.getRadius();
      popular = location.isPopular();
      justResolved = location.justResolved;
      autocomplete = location.autocomplete;
      alias = location.alias;
    }

    public Builder() {
    }

    public Builder radius(double radius) {
      this.radius = radius;
      return this;
    }

    public Builder alias(@Nullable String alias) {
      this.alias = alias;
      return this;
    }

    public Builder popular(boolean popular) {
      this.popular = popular;
      return this;
    }

    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    public Builder wasJustResolved(boolean resolved) {
      this.justResolved = resolved;
      return this;
    }

    public Builder autocomplete(boolean autocomplete) {
      this.autocomplete = autocomplete;
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

  public class ScalarDistanceRequest {
    private final double distance;
    public ScalarDistanceRequest(double distance) {
      this.distance = distance;
    }

    public boolean milesOf(Location other) {
      double actual = LatLngTool.distance(latLng, other.latLng, LengthUnit.MILE);
      return actual < distance;
    }
  }
}
