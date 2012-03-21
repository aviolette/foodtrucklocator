package foodtruck.model;

import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * Static information about a food truck.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class Truck extends ModelEntity {
  private final String id;
  private final String name;
  private final String twitterHandle;
  private final String url;
  private final String iconUrl;
  private final Set<String> categories;
  private final String description;
  private final String foursquareUrl;
  private final boolean twittalyzer;
  private final String defaultCity;
  private final String facebook;
  private final Pattern matchOnlyIf;
  private final boolean inactive;
  private @Nullable final String calendarUrl;

  private Truck(Builder builder) {
    super(builder.key);
    this.id = builder.id;
    this.name = builder.name;
    this.twitterHandle = builder.twitter;
    this.url = builder.url;
    this.iconUrl = builder.iconUrl;
    this.categories = builder.categories;
    this.description = builder.description;
    this.foursquareUrl = builder.foursquareUrl;
    this.twittalyzer = builder.twittalyzer;
    this.defaultCity = builder.defaultCity;
    this.facebook = builder.facebook;
    this.matchOnlyIf = builder.matchOnlyIf;
    this.inactive = builder.inactive;
    this.calendarUrl = builder.calendarUrl;
  }

  public @Nullable String getFoursquareUrl() {
    return foursquareUrl;
  }

  public String getDefaultCity() {
    return defaultCity;
  }

  public String getName() {
    return name;
  }

  public @Nullable String getCalendarUrl() {
    return calendarUrl;
  }

  public String getCategoryList() {
    return Joiner.on(",").join(categories);
  }

  public Set<String> getCategories() {
    return categories;
  }

  public @Nullable String getDescription() {
    return description;
  }

  public String getTwitterHandle() {
    return twitterHandle;
  }

  public String getUrl() {
    return url;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public String getId() {
    return id;
  }

  public boolean isUsingTwittalyzer() {
    return twittalyzer;
  }

  public @Nullable String getFacebook() {
    return facebook;
  }

  public boolean isInactive() {
    return inactive;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, name, url, iconUrl, twitterHandle, inactive);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null || !(o instanceof Truck)) {
      return false;
    }
    Truck truck = (Truck) o;
    return id.equals(truck.id) && name.equals(truck.name) && iconUrl.equals(truck.iconUrl) &&
        Objects.equal(twitterHandle, truck.twitterHandle) && Objects.equal(url, truck.url)
        && inactive == truck.inactive;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("id", id)
        .add("name", name)
        .add("url", url)
        .add("iconUrl", iconUrl)
        .add("twitterHandle", twitterHandle)
        .add("foursquareUrl", foursquareUrl)
        .add("uses twittalyzer", twittalyzer)
        .add("facebook URI", facebook)
        .add("inactive", inactive)
        .toString();
  }

  public @Nullable String getMatchOnlyIfString() {
    return matchOnlyIf == null ? null : matchOnlyIf.toString();
  }

  public @Nullable Pattern getMatchOnlyIf() {
    return matchOnlyIf;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String id;
    private String name;
    private @Nullable String url;
    private String iconUrl;
    private @Nullable String twitter;
    public Set<String> categories = ImmutableSet.of();
    public String description;
    private String foursquareUrl;
    private boolean twittalyzer;
    private String defaultCity = "Chicago, IL";
    private String facebook;
    private Pattern matchOnlyIf;
    private boolean inactive;
    private Object key;
    private @Nullable String calendarUrl;

    public Builder() {
    }

    public Builder(Truck truck) {
      this.id = truck.id;
      this.name = truck.name;
      this.url = truck.url;
      this.iconUrl = truck.iconUrl;
      this.twitter = truck.twitterHandle;
      this.categories = truck.categories;
      this.description = truck.description;
      this.foursquareUrl = truck.foursquareUrl;
      this.twittalyzer = truck.twittalyzer;
      this.defaultCity = truck.defaultCity;
      this.facebook = truck.facebook;
      this.matchOnlyIf = truck.matchOnlyIf;
      this.inactive = truck.inactive;
      this.calendarUrl = truck.calendarUrl;
      this.key = truck.key;
    }

    public Builder key(Object key) {
      this.key = key;
      return this;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder url(@Nullable String url) {
      this.url = url;
      return this;
    }

    public Builder iconUrl(String iconUrl) {
      this.iconUrl = iconUrl;
      return this;
    }

    public Builder categories(ImmutableSet<String> categories) {
      this.categories = categories;
      return this;
    }

    public Builder matchOnlyIf(@Nullable String regex) {
      if (!Strings.isNullOrEmpty(regex)) {
        this.matchOnlyIf = Pattern.compile(regex);
      } else {
        this.matchOnlyIf = null;
      }
      return this;
    }

    public Builder defaultCity(String defaultCity) {
      this.defaultCity = defaultCity;
      return this;
    }

    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    public Builder twitterHandle(@Nullable String twitter) {
      this.twitter = twitter;
      return this;
    }

    public Builder foursquareUrl(@Nullable String foursquare) {
      this.foursquareUrl = foursquare;
      return this;
    }

    public Builder useTwittalyzer(boolean twittalyzer) {
      this.twittalyzer = twittalyzer;
      return this;
    }

    public Truck build() {
      return new Truck(this);
    }

    public Builder facebook(@Nullable String facebook) {
      this.facebook = facebook;
      return this;
    }

    public Builder calendarUrl(@Nullable String calendarUrl) {
      this.calendarUrl = calendarUrl;
      return this;
    }

    public Builder inactive(Boolean inactive) {
      this.inactive = inactive;
      return this;
    }
  }
}
