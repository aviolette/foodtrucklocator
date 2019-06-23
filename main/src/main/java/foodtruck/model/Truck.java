package foodtruck.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkState;

/**
 * Static information about a food truck.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Truck extends ModelEntity implements Serializable {
  private static final Joiner BLACKLIST_JOINER = Joiner.on("; ");
  private String id;
  private String name;
  private String twitterHandle;
  private String url;
  private String iconUrl;
  private ImmutableSet<String> categories;
  private String description;
  private String foursquareUrl;
  private boolean twittalyzer;
  private String defaultCity;
  private String facebook;
  private Pattern matchOnlyIf;
  private Pattern donotMatchIf;
  private boolean inactive;
  private @Nullable String calendarUrl;
  private @Nullable String email;
  private @Nullable String phone;
  private @Nullable DateTime muteUntil;
  private String yelpSlug;
  private @Nullable String facebookPageId;
  private @Nullable Stats stats;
  private boolean hidden;
  private ImmutableSet<String> beaconnaiseEmails;
  private @Nullable String previewIcon;
  private boolean displayEmailPublicly;
  private String instagramId;
  private @Nullable String fullsizeImage;
  private int timezoneAdjustment;
  private boolean scanFacebook;
  private String lastScanned;
  private int fleetSize;
  private String backgroundImage;
  private String backgroundImageLarge;
  private @Nullable String menuUrl;
  private List<String> blacklistLocationNames;
  private @Nullable String phoneticMarkup;
  private ImmutableList<String> phoneticAliases;
  private @Nullable String twitterToken;
  private @Nullable String twitterTokenSecret;
  private boolean neverLinkTwitter;
  private boolean postDailySchedule;
  private boolean postWeeklySchedule;
  private boolean postAtNewStop;
  private @Nullable String facebookAccessToken;
  private @Nullable DateTime facebookAccessTokenExpires;
  private boolean notifyOfLocationChanges;
  private boolean disableBeaconsUntilLunchtime;
  private boolean notifyWhenLeaving;
  private boolean notifyWhenDeviceIssues;
  private @Nullable String drupalCalendar;
  private @Nullable String icalCalendar;
  private @Nullable String squarespaceCalendar;

  // For serialization (for storage in memcached)
  private Truck() {
  }

  private Truck(Builder builder) {
    super(builder.id);
    this.id = builder.id;
    this.name = builder.name;
    this.twitterHandle = builder.twitter;
    this.url = builder.url;
    this.iconUrl = builder.iconUrl;
    this.categories = ImmutableSet.copyOf(builder.categories);
    this.description = builder.description;
    this.foursquareUrl = builder.foursquareUrl;
    this.twittalyzer = builder.twittalyzer;
    this.defaultCity = builder.defaultCity;
    this.facebook = builder.facebook;
    this.facebookPageId = builder.facebookPageId;
    this.matchOnlyIf = builder.matchOnlyIf;
    this.inactive = builder.inactive;
    this.calendarUrl = builder.calendarUrl;
    this.donotMatchIf = builder.donotMatchIf;
    this.email = builder.email;
    this.phone = builder.phone;
    this.muteUntil = builder.muteUntil;
    this.yelpSlug = builder.yelpSlug;
    this.stats = builder.stats;
    this.hidden = builder.hidden;
    this.beaconnaiseEmails = ImmutableSet.copyOf(builder.beaconnaiseEmails);
    this.previewIcon = builder.previewIcon;
    this.displayEmailPublicly = builder.displayEmailPublicly;
    this.instagramId = builder.instagramId;
    this.fullsizeImage = builder.fullsizeImage;
    this.timezoneAdjustment = builder.timezoneAdjustment;
    this.scanFacebook = builder.scanFacebook;
    this.lastScanned = builder.lastScanned;
    this.fleetSize = builder.fleetSize;
    this.backgroundImage = builder.backgroundImage;
    this.backgroundImageLarge = builder.backgroundImageLarge;
    this.menuUrl = builder.menuUrl;
    this.blacklistLocationNames = builder.blacklistLocationNames;
    this.phoneticMarkup = builder.phoneticMarkup;
    this.phoneticAliases = ImmutableList.copyOf(builder.phoneticAliases);
    this.twitterToken = builder.twitterToken;
    this.twitterTokenSecret = builder.twitterTokenSecret;
    this.neverLinkTwitter = builder.neverLinkTwitter;
    this.postDailySchedule = builder.postDailySchedule;
    this.postWeeklySchedule = builder.postWeeklySchedule;
    this.postAtNewStop = builder.postAtNewStop;
    this.facebookAccessToken = builder.facebookAccessToken;
    this.facebookAccessTokenExpires = builder.facebookAccessTokenExpires;
    this.notifyOfLocationChanges = builder.notifyOfLocationChanges;
    this.disableBeaconsUntilLunchtime = builder.disableBeaconsUntilLunchtime;
    this.notifyWhenDeviceIssues = builder.notifyWhenDeviceIssues;
    this.notifyWhenLeaving = builder.notifyWhenLeaving;
    this.drupalCalendar = builder.drupalCalendar;
    this.icalCalendar = builder.icalCalendar;
    this.squarespaceCalendar = builder.squarespaceCalendar;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Truck t) {
    return new Builder(t);
  }

  public static String canonize(String name) {
    name = name.toLowerCase();
    if (name.startsWith("the ")) {
      name = name.substring(4);
    }
    return name;
  }

  public Builder append() {
    return new Builder(this);
  }

  public boolean isNotifyWhenLeaving() {
    return notifyWhenLeaving;
  }

  public boolean isNotifyWhenDeviceIssues() {
    return notifyWhenDeviceIssues;
  }

  public boolean isNotifyOfLocationChanges() {
    return notifyOfLocationChanges;
  }

  @Nullable
  public String getFacebookAccessToken() {
    return facebookAccessToken;
  }

  @Nullable
  public DateTime getFacebookAccessTokenExpires() {
    return facebookAccessTokenExpires;
  }

  @Nullable
  public String getTwitterToken() {
    return twitterToken;
  }

  @Nullable
  public String getTwitterTokenSecret() {
    return twitterTokenSecret;
  }

  public boolean isNeverLinkTwitter() {
    return neverLinkTwitter;
  }

  public boolean isPostDailySchedule() {
    return postDailySchedule;
  }

  public boolean isPostWeeklySchedule() {
    return postWeeklySchedule;
  }

  public boolean isMatchesMorningStops() {
    return categories.contains("Breakfast");
  }

  public boolean isPostAtNewStop() {
    return postAtNewStop;
  }

  public boolean getHasSocialMediaCredentials() {
    return getHasTwitterCredentials();
  }

  public boolean getHasFacebookCredentials() {
    return !Strings.isNullOrEmpty(facebookAccessToken);
  }

  public boolean getHasTwitterCredentials() {
    return !(Strings.isNullOrEmpty(twitterToken) || Strings.isNullOrEmpty(twitterTokenSecret));
  }

  public ImmutableList<String> getPhoneticAliases() {
    return phoneticAliases;
  }

  public String getPhoneticAliasCSV() {
    return Joiner.on(", ")
        .join(phoneticAliases);
  }

  @Nullable
  public String getPhoneticMarkup() {
    return phoneticMarkup;
  }

  public String getNameInSSML() {
    return MoreObjects.firstNonNull(phoneticMarkup, name);
  }

  @Nullable
  public String getIcalCalendar() {
    return icalCalendar;
  }

  @Nullable
  public String getSquarespaceCalendar() {
    return squarespaceCalendar;
  }

  @Nullable
  public String getDrupalCalendar() {
    return drupalCalendar;
  }

  public List<String> getBlacklistLocationNames() {
    return blacklistLocationNames;
  }

  public String getBlacklistLocationsList() {
    return BLACKLIST_JOINER.join(blacklistLocationNames);
  }

  public @Nullable String getMenuUrl() {
    return menuUrl;
  }

  public @Nullable String getFullsizeImage() {
    return fullsizeImage;
  }

  public @Nullable String getInstagramId() {
    return instagramId;
  }

  public boolean isDisplayEmailPublicly() {
    return this.displayEmailPublicly;
  }

  public boolean isVisible() {
    return !isHidden();
  }

  public boolean isHidden() {
    return hidden;
  }

  public @Nullable String getFoursquareUrl() {
    return foursquareUrl;
  }

  public String getDefaultCity() {
    return defaultCity;
  }

  public @Nullable String getPreviewIcon() {
    return previewIcon;
  }

  public Url getPreviewIconUrl() {
    if (Strings.isNullOrEmpty(previewIcon)) {
      return new Url("https://storage.googleapis.com/truckpreviews/truck_holder.svg");
    }
    return new Url(previewIcon);
  }

  public String getName() {
    return name;
  }

  public String getYelpSlug() {
    return yelpSlug;
  }

  public boolean isPopupVendor() {
    return categories.contains("Popup");
  }

  public @Nullable String getCalendarUrl() {
    return calendarUrl;
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getCategoryList() {
    return Joiner.on(", ").join(categories);
  }

  public String getPhoneticAliasesList() {
    return Joiner.on(", ")
        .join(phoneticAliases);
  }

  public Set<String> getCategories() {
    return categories;
  }

  public Set<String> getBeaconnaiseEmails() {
    return beaconnaiseEmails;
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getBeaconnaiseList() {
    return Joiner.on(", ").join(beaconnaiseEmails);
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

  public @Nullable Url getUrlObj() {
    return new Url(url);
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public Url getIconUrlObj() {
    if (Strings.isNullOrEmpty(iconUrl)) {
      return null;
    }
    return new Url(iconUrl);
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

  public @Nullable String getFacebookHandle() {
    if (facebook == null || facebook.lastIndexOf('/') != 0) {
      return facebook;
    } else {
      return "@" + facebook.substring(1);
    }

  }

  public @Nullable String getFacebookPageId() {
    return facebookPageId;
  }

  public boolean isInactive() {
    return inactive;
  }

  public @Nullable String getPublicEmail() {
    return displayEmailPublicly ? getEmail() : null;
  }

  public @Nullable String getEmail() {
    return email;
  }

  public @Nullable String getPhone() {
    return phone;
  }

  public boolean shouldAnalyzeStories() {
    return !Strings.isNullOrEmpty(twitterHandle) || !Strings.isNullOrEmpty(facebook);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, name, url, iconUrl, twitterHandle, inactive);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Truck)) {
      return false;
    }
    Truck truck = (Truck) o;
    return id.equals(truck.id) && name.equals(truck.name) && Objects.equal(iconUrl, truck.iconUrl) &&
        Objects.equal(twitterHandle, truck.twitterHandle) && Objects.equal(url, truck.url)
        && inactive == truck.inactive;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("name", name)
        .add("url", url)
        .add("phone", phone)
        .add("iconUrl", iconUrl)
        .add("twitterHandle", twitterHandle)
        .add("foursquareUrl", foursquareUrl)
        .add("uses twittalyzer", twittalyzer)
        .add("facebook URI", facebook)
        .add("Yelp Slug", yelpSlug)
        .add("inactive", inactive)
        .add("instagramId", instagramId)
        .add("muteUntil", muteUntil)
        .add("scanFacebook", scanFacebook)
        .toString();
  }

  public @Nullable String getMatchOnlyIfString() {
    return matchOnlyIf == null ? null : matchOnlyIf.toString();
  }

  public @Nullable Pattern getMatchOnlyIf() {
    return matchOnlyIf;
  }

  public @Nullable String getDonotMatchIfString() {
    return donotMatchIf == null ? null : donotMatchIf.toString();
  }

  public @Nullable Pattern getDonotMatchIf() {
    return donotMatchIf;
  }

  @Override public void validate() throws IllegalStateException {
    super.validate();
    checkState(!Strings.isNullOrEmpty(id), "ID cannot be unspecified");
    checkState(!Strings.isNullOrEmpty(name), "Name must be specified");
  }

  public @Nullable DateTime getMuteUntil() {
    return muteUntil;
  }

  @SuppressWarnings("UnusedDeclaration")
  public boolean isMuted() {
    return muteUntil != null && muteUntil.isAfter(new DateTime());
  }

  public @Nullable Stats getStats() {
    return stats;
  }

  public boolean match(String tweet) {
    Pattern p = getMatchOnlyIf();
    if (p != null) {
      Matcher m = p.matcher(tweet.toLowerCase());
      return m.find();
    }
    p = getDonotMatchIf();
    if (p != null) {
      Matcher m = p.matcher(tweet.toLowerCase());
      return !m.find();
    }
    return true;
  }

  public int getTimezoneAdjustment() {
    return timezoneAdjustment;
  }

  public boolean isSavory() {
    if (categories.contains("Sandwiches") || categories.contains("Lunch")) {
      return true;
    } else if (categories.contains("Dogs") || categories.contains("Cupcakes") ||
        categories.contains("Dessert") || categories.contains("Donuts")) {
      return false;
    }
    return true;
  }

  public boolean getScanFacebook() {
    return scanFacebook;
  }

  public String getLastScanned() {
    return lastScanned;
  }

  public Set<String> getPublicCategories() {
    return publicCategories();
  }

  public Set<String> publicCategories() {
    return categories.stream()
        .filter(input -> !(input.equals("Lunch") || input.equals("HasStore") ||
            input.equals("1HRStops") || input.equals("MorningSquatter") ||
            input.equals("AssumeNoTimeEqualsLunch") || input.equals("Chicago") ||
            input.equals("Burbs")))
        .collect(Collectors.toSet());
  }

  public int getFleetSize() {
    return fleetSize;
  }

  public String getBackgroundImage() {
    return backgroundImage;
  }

  public @Nullable Url getBackgroundImageUrl() {
    if (Strings.isNullOrEmpty(backgroundImage)) {
      return null;
    }
    return new Url(backgroundImage);
  }

  public @Nullable Url getBiggestBackgroundImageUrl() {
    if (Strings.isNullOrEmpty(backgroundImageLarge)) {
      return getBackgroundImageUrl();
    }
    return new Url(backgroundImageLarge);
  }

  public String getBackgroundImageLarge() {
    return backgroundImageLarge;
  }

  public @Nullable Url getLargeBackgroundImageUrl() {
    if (Strings.isNullOrEmpty(backgroundImageLarge)) {
      return null;
    }
    return new Url(backgroundImageLarge);
  }

  public String canonicalName() {
    return canonize(getName());
  }

  public boolean getDeriveStopsFromSocialMedia() {
    return isUsingTwittalyzer() || getScanFacebook();
  }

  public String nameForTwitterDisplay() {
    if (Strings.isNullOrEmpty(twitterHandle)) {
      return name;
    }
    return "@" + twitterHandle;
  }

  public static class HasCategoryPredicate implements Predicate<Truck> {
    private String category;

    public HasCategoryPredicate(String category) {
      this.category = category;
    }

    public boolean apply(Truck input) {
      return input.categories.contains(category);
    }
  }

  public static class Stats implements Serializable {
    private DateTime lastUpdated;
    private DateTime lastSeen;
    private DateTime firstSeen;
    private long totalStops;
    private long stopsThisYear;
    private Location whereLastSeen;
    private Location whereFirstSeen;
    private int numberOfDaysOutThisYear;

    public Stats(Builder builder) {
      this.lastUpdated = builder.lastUpdated;
      this.lastSeen = builder.lastSeen;
      this.totalStops = builder.totalStops;
      this.stopsThisYear = builder.stopsThisYear;
      this.whereLastSeen = builder.whereLastSeen;
      this.numberOfDaysOutThisYear = builder.numberOfDaysOutThisYear;
      this.firstSeen = builder.firstSeen;
      this.whereFirstSeen = builder.whereFirstSeen;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static Builder builder(Stats stats) {
      return new Builder(stats);
    }

    public DateTime getFirstSeen() {
      return firstSeen;
    }

    public Location getWhereFirstSeen() {
      return whereFirstSeen;
    }

    public long getStopsThisYear() {
      return stopsThisYear;
    }


    public DateTime getLastUpdated() {
      return lastUpdated;
    }

    public long getTotalStops() {
      return totalStops;
    }

    public @Nullable DateTime getLastSeen() {
      return lastSeen;
    }

    public @Nullable Location getWhereLastSeen() {
      return whereLastSeen;
    }

    public static class Builder {

      private DateTime lastUpdated = new DateTime(2009, 1, 1, 1, 1, 1, 1);
      private @Nullable DateTime lastSeen;
      private long totalStops;
      private long stopsThisYear;
      private @Nullable Location whereLastSeen;
      private int numberOfDaysOutThisYear;
      private @Nullable DateTime firstSeen;
      private Location whereFirstSeen;

      public Builder() {}

      public Builder(Stats stats) {
        this.lastUpdated = stats.getLastUpdated();
        this.lastSeen = stats.getLastSeen();
        this.totalStops = stats.getTotalStops();
        this.stopsThisYear = stats.getStopsThisYear();
        this.whereLastSeen = stats.getWhereLastSeen();
        this.numberOfDaysOutThisYear = stats.numberOfDaysOutThisYear;
        this.firstSeen = stats.getFirstSeen();
        this.whereFirstSeen = stats.getWhereFirstSeen();
      }

      public Builder lastUpdate(DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
      }

      public Builder totalStops(long totalStops) {
        this.totalStops = totalStops;
        return this;
      }

      public Builder whereFirstSeen(@Nullable Location whereFirstSeen) {
        this.whereFirstSeen = whereFirstSeen;
        return this;
      }

      public Builder firstSeen(@Nullable DateTime firstSeen) {
        this.firstSeen = firstSeen;
        return this;
      }

      public Builder lastSeen(@Nullable DateTime lastSeen) {
        this.lastSeen = lastSeen;
        return this;
      }

      public Builder stopsThisYear(long stopsThisYear) {
        this.stopsThisYear = stopsThisYear;
        return this;
      }

      public Builder whereLastSeen(@Nullable Location whereLastSeen) {
        this.whereLastSeen = whereLastSeen;
        return this;
      }

      public Builder numberOfDaysOutThisYear(int daysOut) {
        this.numberOfDaysOutThisYear = daysOut;
        return this;
      }

      public Stats build() {
        return new Stats(this);
      }
    }
  }

  public static class Builder {
    public Set<String> categories = ImmutableSet.of();
    public String description;
    public Set<String> beaconnaiseEmails = ImmutableSet.of();
    public boolean disableBeaconsUntilLunchtime;
    private String id;
    private String name;
    private @Nullable String url;
    private String iconUrl = "https://storage.googleapis.com/truckpreviews/truck_holder.svg";
    private @Nullable String twitter;
    private String foursquareUrl;
    private boolean twittalyzer;
    private String defaultCity = "Chicago, IL";
    private String facebook;
    private Pattern matchOnlyIf;
    private boolean inactive;
    private @Nullable String calendarUrl;
    private Pattern donotMatchIf;
    private @Nullable String email;
    private @Nullable String phone;
    private @Nullable DateTime muteUntil;
    private @Nullable String yelpSlug;
    private @Nullable String facebookPageId;
    private @Nullable Stats stats;
    private boolean hidden;
    private @Nullable String previewIcon;
    private boolean displayEmailPublicly;
    private @Nullable String instagramId;
    private @Nullable String fullsizeImage;
    private int timezoneAdjustment = 0;
    private boolean scanFacebook;
    private String lastScanned;
    private int fleetSize;
    private String backgroundImage;
    private String backgroundImageLarge;
    private @Nullable String menuUrl;
    private List<String> blacklistLocationNames = ImmutableList.of();
    private @Nullable String phoneticMarkup;
    private List<String> phoneticAliases = ImmutableList.of();
    private @Nullable String twitterToken;
    private @Nullable String twitterTokenSecret;
    private boolean neverLinkTwitter;
    private boolean postDailySchedule;
    private boolean postWeeklySchedule;
    private boolean postAtNewStop;
    private @Nullable String facebookAccessToken;
    private @Nullable DateTime facebookAccessTokenExpires;
    private boolean notifyOfLocationChanges;
    private boolean notifyWhenLeaving;
    private boolean notifyWhenDeviceIssues;
    private @Nullable String drupalCalendar;
    private @Nullable String icalCalendar;
    private @Nullable String squarespaceCalendar;

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
      this.donotMatchIf = truck.donotMatchIf;
      this.matchOnlyIf = truck.matchOnlyIf;
      this.inactive = truck.inactive;
      this.calendarUrl = truck.calendarUrl;
      this.muteUntil = truck.muteUntil;
      this.yelpSlug = truck.yelpSlug;
      this.facebookPageId = truck.facebookPageId;
      this.stats = truck.stats;
      this.hidden = truck.hidden;
      this.phone = truck.phone;
      this.email = truck.email;
      this.previewIcon = truck.previewIcon;
      this.beaconnaiseEmails = truck.beaconnaiseEmails;
      this.displayEmailPublicly = truck.displayEmailPublicly;
      this.instagramId = truck.instagramId;
      this.fullsizeImage = truck.fullsizeImage;
      this.timezoneAdjustment = truck.timezoneAdjustment;
      this.scanFacebook = truck.scanFacebook;
      this.lastScanned = truck.lastScanned;
      this.fleetSize = truck.fleetSize;
      this.backgroundImage = truck.backgroundImage;
      this.backgroundImageLarge = truck.backgroundImageLarge;
      this.menuUrl = truck.menuUrl;
      this.blacklistLocationNames = truck.blacklistLocationNames;
      this.phoneticMarkup = truck.phoneticMarkup;
      this.phoneticAliases = truck.phoneticAliases;
      this.twitterToken = truck.twitterToken;
      this.twitterTokenSecret = truck.twitterTokenSecret;
      this.neverLinkTwitter = truck.neverLinkTwitter;
      this.postDailySchedule = truck.postDailySchedule;
      this.postWeeklySchedule = truck.postWeeklySchedule;
      this.postAtNewStop = truck.postAtNewStop;
      this.facebookAccessToken = truck.facebookAccessToken;
      this.facebookAccessTokenExpires = truck.facebookAccessTokenExpires;
      this.notifyOfLocationChanges = truck.notifyOfLocationChanges;
      this.disableBeaconsUntilLunchtime = truck.disableBeaconsUntilLunchtime;
      this.notifyWhenDeviceIssues = truck.notifyWhenDeviceIssues;
      this.notifyWhenLeaving = truck.notifyWhenLeaving;
      this.drupalCalendar = truck.drupalCalendar;
      this.icalCalendar = truck.icalCalendar;
      this.squarespaceCalendar = truck.squarespaceCalendar;
    }

    public Builder squarespaceCalendar(@Nullable String squarespaceCalendar) {
      this.squarespaceCalendar = squarespaceCalendar;
      return this;
    }

    public Builder icalCalendar(@Nullable String icalCalendar) {
      this.icalCalendar = icalCalendar;
      return this;
    }

    public Builder drupalCalendar(@Nullable String drupalCalendar) {
      this.drupalCalendar = drupalCalendar;
      return this;
    }

    public Builder notifyWhenDeviceIssues(boolean notify) {
      this.notifyWhenDeviceIssues = notify;
      return this;
    }

    public Builder notifyWhenLeaving(boolean notify) {
      this.notifyWhenLeaving = notify;
      return this;
    }

    public Builder notifyOfLocationChanges(boolean notify) {
      this.notifyOfLocationChanges = notify;
      return this;
    }

    public Builder phoneticMarkup(String markup) {
      this.phoneticMarkup = markup;
      return this;
    }

    public Builder blacklistLocationNames(List<String> locationNames) {
      this.blacklistLocationNames = locationNames;
      return this;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder fullsizeImage(@Nullable String fullsizeImage) {
      this.fullsizeImage = fullsizeImage;
      return this;
    }

    public Builder instagramId(@Nullable String instagramId) {
      this.instagramId = instagramId;
      return this;
    }

    public Builder displayEmailPublicly(boolean displayEmailPublicly) {
      this.displayEmailPublicly = displayEmailPublicly;
      return this;
    }

    public Builder hidden(boolean hidden) {
      this.hidden = hidden;
      return this;
    }

    public Builder lastScanned(String lastScannedId) {
      this.lastScanned = lastScannedId;
      return this;
    }

    public Builder beaconnaiseEmails(Set<String> emails) {
      this.beaconnaiseEmails = emails;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder previewIcon(String previewIcon) {
      this.previewIcon = previewIcon;
      return this;
    }

    public Builder url(@Nullable String url) {
      this.url = url;
      return this;
    }

    public Builder muteUntil(@Nullable DateTime muteUntil) {
      this.muteUntil = muteUntil;
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

    public Builder facebookPageId(@Nullable String facebookPageId) {
      this.facebookPageId = facebookPageId;
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

    public Builder email(@Nullable String email) {
      this.email = email;
      return this;
    }

    public Builder phone(@Nullable String phone) {
      this.phone = phone;
      return this;
    }

    public Builder yelpSlug(@Nullable String yelpSlug) {
      this.yelpSlug = yelpSlug;
      return this;
    }

    public Builder scanFacebook(boolean scanFacebook) {
      this.scanFacebook = scanFacebook;
      return this;
    }

    public Builder donotMatchIf(@Nullable String regex) {
      if (!Strings.isNullOrEmpty(regex)) {
        this.donotMatchIf = Pattern.compile(regex);
      } else {
        this.donotMatchIf = null;
      }
      return this;
    }

    public Builder stats(Stats stats) {
      this.stats = stats;
      return this;
    }

    public Builder timezoneOffset(int timezoneAdjustment) {
      this.timezoneAdjustment = timezoneAdjustment;
      return this;
    }

    public Builder fleetSize(int size) {
      this.fleetSize = size;
      return this;
    }

    public Builder phoneticAliases(List<String> phoneticAliases) {
      this.phoneticAliases = phoneticAliases;
      return this;
    }

    public Builder backgroundImage(String image) {
      this.backgroundImage = image;
      return this;
    }

    public Builder backgroundImageLarge(String image) {
      this.backgroundImageLarge = image;
      return this;
    }

    public Builder menuUrl(String menuUrl) {
      this.menuUrl = menuUrl;
      return this;
    }

    public Builder normalizePhone(String phone) {
      // only really works for american locale, but I'll cross that bridge later
      if (phone.length() < 10) {
        this.phone = phone;
      } else {
        this.phone = phone.replaceAll("\\(|\\)|\\-|\\+|\\.| ", "");
        if (this.phone.length() == 10) {
          this.phone = this.phone.substring(0, 3) + "-" + this.phone.substring(3, 6) + "-" + this.phone.substring(6, 10);
        }
      }
      return this;
    }

    public Builder twitterToken(String twitterToken) {
      this.twitterToken = twitterToken;
      return this;
    }

    public Builder twitterTokenSecret(String twitterTokenSecret) {
      this.twitterTokenSecret = twitterTokenSecret;
      return this;
    }

    public Builder neverLinkTwitter(boolean neverLinkTwitter) {
      this.neverLinkTwitter = neverLinkTwitter;
      return this;
    }

    public Builder postDailySchedule(boolean postDailySchedule) {
      this.postDailySchedule = postDailySchedule;
      return this;
    }

    public Builder postWeeklySchedule(boolean postWeeklySchedule) {
      this.postWeeklySchedule = postWeeklySchedule;
      return this;
    }

    public Builder postAtNewStop(boolean postAtNewStop) {
      this.postAtNewStop = postAtNewStop;
      return this;
    }

    public Builder facebookAccessToken(String facebookAccessToken) {
      this.facebookAccessToken = facebookAccessToken;
      return this;
    }

    public Builder facebookAccessTokenExpires(DateTime expires) {
      this.facebookAccessTokenExpires = expires;
      return this;
    }

    public Builder clearTwitterCredentials() {
      this.neverLinkTwitter = true;
      this.twitterTokenSecret = null;
      this.twitterToken = null;
      return this;
    }
  }
}
