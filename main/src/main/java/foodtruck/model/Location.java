package foodtruck.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;


/**
 * Latitude and Longitude.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class Location extends ModelEntity implements Serializable {
  private static final String UNKNOWN = "UNKNOWN";

  private static final long serialVersionUID = 1L;
  private LatLng latLng;
  private String name;
  private boolean valid;
  private @Nullable String description;
  private @Nullable String url;
  private boolean eventSpecific;
  private double radius;
  private boolean popular;
  private boolean justResolved;
  private boolean autocomplete;
  private @Nullable String alias;
  private @Nullable String twitterHandle;
  private boolean designatedStop;
  private boolean hasBooze;
  private @Nullable String ownedBy;
  private int radiateTo;
  private @Nullable String phoneNumber;
  private @Nullable String email;
  private @Nullable String facebookUri;
  private boolean closed;
  private @Nullable Url imageUrl;
  private @Nullable String eventCalendarUrl;
  private ImmutableSet<String> managerEmails;
  private boolean alexaProvided;
  private @Nullable String createdBy;
  private boolean blacklistedFromCalendarSearch;
  private @Nullable String city;
  private @Nullable String neighborhood;

  // For serializable
  public Location() {
  }

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
    twitterHandle = builder.twitterHandle;
    designatedStop = builder.designatedStop;
    hasBooze = builder.hasBooze;
    ownedBy = builder.ownedBy;
    radiateTo = builder.radiateTo;
    phoneNumber = builder.phoneNumber;
    email = builder.email;
    facebookUri = builder.facebookUri;
    closed = builder.closed;
    url = builder.url;
    imageUrl = builder.imageUrl;
    eventCalendarUrl = builder.eventCalendarUrl;
    managerEmails = ImmutableSet.copyOf(builder.managerEmails);
    alexaProvided = builder.alexaProvided;
    createdBy = builder.createdBy;
    blacklistedFromCalendarSearch = builder.blacklistedFromCalendarSearch;
    city = builder.city;
    neighborhood = builder.neighborhood;
  }

  @Nullable
  public String getNeighborhood() {
    return neighborhood;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Location loc) {
    return new Builder(loc);
  }

  @Nullable
  public String getCreatedBy() {
    return createdBy;
  }

  @Nullable
  public String getEventCalendarUrl() {
    return eventCalendarUrl;
  }

  @Nullable
  public Url getImageUrl() {
    return imageUrl;
  }

  public boolean isAlexaProvided() {
    return alexaProvided;
  }

  public boolean isClosed() {
    return closed;
  }

  @Nullable
  public String getPhoneNumber() {
    return phoneNumber;
  }

  @Nullable
  public String getEmail() {
    return email;
  }

  @Nullable
  public String getFacebookUri() {
    return facebookUri;
  }

  public boolean isHasBooze() {
    return hasBooze;
  }

  public boolean isDesignatedStop() {
    return designatedStop;
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

  @Nullable
  public String getTwitterHandle() {
    return this.twitterHandle;
  }

  public Location wasJustResolved() {
    return Location.builder(this)
        .wasJustResolved(true)
        .build();
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

  public boolean withinToleranceOf(Location location) {
    return within(0.05).milesOf(location);
  }

  @Nullable
  public String getDescription() {
    return this.description;
  }

  // TODO: this probably should be refactored out of here

  @Nullable
  public String getUrl() {
    return url;
  }

  /**
   * Return the truck that owns this location (i.e. a restaurant)
   * @return the truck that owns this location or null if it's not owned
   */
  @Nullable
  public String getOwnedBy() {
    return ownedBy;
  }

  /**
   * Return true if the location has been properly resolved.
   */
  public boolean isResolved() {
    return valid && latLng.getLatitude() != 0 && latLng.getLongitude() != 0;
  }

  public boolean containedWithRadiusOf(Location loc) {
    return equals(loc) || loc.getRadius() != 0 && within(loc.getRadius()).milesOf(loc);
  }

  public boolean sameName(Location loc) {
    return !UNKNOWN.equals(name) && name.equals(loc.getName());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("Lat/Lng", latLng)
        .add("Name", name)
        // appengine throws Method undefined errors 'cause of this...not sure why
        .add("Radius", String.valueOf(radius))
        .add("Owned by", ownedBy)
        .add("Created by ", createdBy)
        .add("Alias for", alias)
        .add("City", city)
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

  public Location withKey(Object key) {
    return builder(this).key(key)
        .build();
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
    return Location.builder(this)
        .name(name)
        .build();
  }

  @Nullable
  public String getAlias() {
    return alias;
  }

  public int getRadiateTo() {
    return radiateTo;
  }

  public ImmutableSet<String> getManagerEmails() {
    return managerEmails;
  }

  public String getShortenedName() {
    String shortened = name;
    if (Strings.isNullOrEmpty(name)) {
      return UNKNOWN;
    }
    if (shortened.matches(".*\\d{5}, USA")) {
      shortened = shortened.substring(0, shortened.length() - 11);
    }
    if (shortened.endsWith(", Chicago, IL")) {
      return shortened.substring(0, shortened.length() - 13);
    }
    return shortened;
  }

  public Comparator<Location> distanceFromComparator() {
    return new Comparator<Location>() {
      @Override
      public int compare(Location o1, Location o2) {
        double o1Val = o1.distanceFrom(Location.this),
            o2Val = o2.distanceFrom(Location.this);
        if (o1Val == o2Val) {
          return 0;
        } else if (o1Val < o2Val) {
          return -1;
        } else {
          return 1;
        }
      }
    };
  }

  public boolean isBlacklistedFromCalendarSearch() {
    return blacklistedFromCalendarSearch;
  }

  public java.util.function.Predicate<Location> rangedPredicate8(final double distance) {
    return (input) -> input != null && input.within(distance).milesOf(Location.this);
  }

  public Predicate<Location> rangedPredicate(final double distance) {
    return new Predicate<Location>() {
      @Override
      public boolean apply(@Nullable Location input) {
        return input != null && input.within(distance)
            .milesOf(Location.this);
      }
    };
  }

  public Location withRadius(double radius) {
    return Location.builder(this).radius(radius).build();
  }

  public String getCity() {
    return city;
  }

  public static class Builder {

    private Object key;
    private double lat;
    private double lng;
    private String name = UNKNOWN;
    private boolean valid = true;
    private @Nullable String description;
    private boolean eventSpecific;
    private @Nullable String url;
    private double radius = 0d;
    private boolean popular;
    private boolean justResolved;
    private boolean autocomplete;
    private @Nullable String alias;
    private @Nullable String twitterHandle;
    private boolean designatedStop;
    private boolean hasBooze;
    private @Nullable String ownedBy;
    private int radiateTo = 0;
    private @Nullable String phoneNumber;
    private @Nullable String email;
    private @Nullable String facebookUri;
    private boolean closed;
    private @Nullable Url imageUrl;
    private @Nullable String eventCalendarUrl;
    private Set<String> managerEmails = ImmutableSet.of();
    private boolean alexaProvided;
    private @Nullable String createdBy;
    private boolean blacklistedFromCalendarSearch;
    private String city;
    private String neighborhood;

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
      twitterHandle = location.twitterHandle;
      designatedStop = location.designatedStop;
      hasBooze = location.hasBooze;
      ownedBy = location.ownedBy;
      radiateTo = location.radiateTo;
      phoneNumber = location.phoneNumber;
      email = location.email;
      facebookUri = location.facebookUri;
      closed = location.closed;
      imageUrl = location.imageUrl;
      eventCalendarUrl = location.eventCalendarUrl;
      managerEmails = location.managerEmails;
      alexaProvided = location.alexaProvided;
      createdBy = location.createdBy;
      blacklistedFromCalendarSearch = location.blacklistedFromCalendarSearch;
      city = location.city;
      neighborhood = location.neighborhood;
    }

    public Builder() {
    }

    public Builder city(String city) {
      this.city = city;
      return this;
    }

    public Builder neighborhood(String neighborhood) {
      this.neighborhood = neighborhood;
      return this;
    }

    public Builder blacklistedFromCalendarSearch(boolean blacklisted) {
      this.blacklistedFromCalendarSearch = blacklisted;
      return this;
    }

    public Builder alexaProvided(boolean alexaProvided) {
      this.alexaProvided = alexaProvided;
      return this;
    }

    public Builder eventCalendarUrl(String eventCalendarUrl) {
      this.eventCalendarUrl = eventCalendarUrl;
      return this;
    }

    public Builder imageUrl(Url imageUrl) {
      this.imageUrl = imageUrl;
      return this;
    }

    public Builder phoneNumber(String phoneNumber) {
      this.phoneNumber = phoneNumber;
      return this;
    }

    public Builder createdBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder closed(boolean closed) {
      this.closed = closed;
      return this;
    }

    public Builder facebookUri(String facebookUri) {
      this.facebookUri = facebookUri;
      return this;
    }

    public Builder radiateTo(int radiateTo) {
      this.radiateTo = radiateTo;
      return this;
    }

    public Builder ownedBy(String ownedBy) {
      this.ownedBy = ownedBy;
      return this;
    }

    public Builder hasBooze(boolean hasBooze) {
      this.hasBooze = hasBooze;
      return this;
    }

    public Builder designatedStop(boolean designatedStop) {
      this.designatedStop = designatedStop;
      return this;
    }

    public Builder radius(double radius) {
      this.radius = radius;
      return this;
    }

    public Builder alias(@Nullable String alias) {
      this.alias = alias;
      return this;
    }

    public Builder twitterHandle(@Nullable String twitterHandle) {
      this.twitterHandle = twitterHandle;
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

    public Builder managerEmails(Set managerEmails) {
      this.managerEmails = managerEmails;
      return this;
    }

    public Location build() {
      return new Location(this);
    }
  }

  @SuppressWarnings("WeakerAccess")
  public class ScalarDistanceRequest {
    private final double distance;

    ScalarDistanceRequest(double distance) {
      this.distance = distance;
    }

    public boolean milesOf(Location other) {
      double actual = LatLngTool.distance(latLng, other.latLng, LengthUnit.MILE);
      return actual < distance;
    }
  }
}
