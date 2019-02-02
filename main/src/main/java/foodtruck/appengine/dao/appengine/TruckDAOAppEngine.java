package foodtruck.appengine.dao.appengine;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.joda.time.DateTimeZone;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static foodtruck.appengine.dao.appengine.Attributes.getDateTime;
import static foodtruck.appengine.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.appengine.dao.appengine.Attributes.getIntProperty;
import static foodtruck.appengine.dao.appengine.Attributes.getListProperty;
import static foodtruck.appengine.dao.appengine.Attributes.getLongProperty;
import static foodtruck.appengine.dao.appengine.Attributes.getSetProperty;
import static foodtruck.appengine.dao.appengine.Attributes.getStringProperty;
import static foodtruck.appengine.dao.appengine.Attributes.setDateProperty;

/**
 * @author aviolette@gmail.com
 * @since 2/26/12
 */
class TruckDAOAppEngine extends AppEngineDAO<String, Truck> implements TruckDAO {

  private static final String TRUCK_KIND = "Store";
  private static final String TRUCK_NAME_FIELD = "name";
  private static final String TRUCK_CANONICAL_NAME = "canonical_name";
  private static final String TRUCK_TWITTER_HANDLE = "twitterHandle";
  private static final String TRUCK_URL = "url";
  private static final String TRUCK_ICON_URL = "iconUrl";
  private static final String TRUCK_DESCRIPTION_FIELD = "descriptionText";
  private static final String TRUCK_FOURSQUARE_URL_FIELD = "foursquareUrl";
  private static final String TRUCK_TWITTALYZER_FIELD = "useTwittalyzer";
  private static final String TRUCK_DEFAULT_CITY_FIELD = "defaultCity";
  private static final String TRUCK_FACEBOOK_FIELD = "facebookUrl";
  private static final String MATCH_REGEX_FIELD = "matchOnlyIf";
  private static final String DONT_MATCH_REGEX_FIELD = "dontMatchIf";
  private static final String INACTIVE_FIELD = "inactive";
  private static final String CATEGORIES_FIELD = "categories";
  private static final String TRUCK_CALENDAR_URL = "calendarUrl";
  private static final String TRUCK_EMAIL = "email";
  private static final String TRUCK_PHONE = "phone";
  private static final String TRUCK_MUTE_UNTIL = "muteUntil";
  private static final String TRUCK_YELP_SLUG = "yelp";
  private static final String TRUCK_FACEBOOK_PAGE_ID = "facebookPageId";
  private static final String TRUCK_STATS_LAST_SEEN_WHEN = "last_seen_when";
  private static final String TRUCK_STATS_LAST_SEEN_WHERE = "last_seen_where";
  private static final String TRUCK_STATS_LAST_UPDATED = "last_updated";
  private static final String TRUCK_STATS_TOTAL_STOPS = "all_stops";
  private static final String TRUCK_STATS_STOPS_THIS_YEAR = "stops_this_year";
  private static final String TRUCK_STATS_LAST_SEEN_WHERE_LAT = "last_seen_lat";
  private static final String TRUCK_STATS_LAST_SEEN_WHERE_LNG = "last_seen_lng";
  private static final String TRUCK_STATS_FIRST_SEEN_WHEN = "first_seen_when";
  private static final String TRUCK_STATS_FIRST_SEEN_WHERE = "first_seen_where";
  private static final String TRUCK_STATS_FIRST_SEEN_WHERE_LAT = "first_seen_lat";
  private static final String TRUCK_STATS_FIRST_SEEN_WHERE_LNG = "first_seen_lng";
  private static final String TRUCK_HIDDEN = "hidden";
  private static final String TRUCK_BEACONNAISE_EMAILS = "beaconnaise_emails";
  private static final String TRUCK_PREVIEW_ICON = "truck_preview_icon";
  private static final String TRUCK_ALLOW_SYSTEM_NOTIFICATIONS = "allow_system_notifications";
  private static final String TRUCK_DISPLAY_EMAIL = "display_email";
  private static final String TRUCK_INSTAGRAM = "instagram";
  private static final String TRUCK_FULL_SIZE = "fullsize_image";
  private static final String TIMEZONE_OFFSET = "timezone_offset";
  private static final String SCAN_FACEBOOK = "scan_facebook";
  private static final String LAST_SCANNED = "last_scanned_facebook";
  private static final String FLEET_SIZE = "fleet_size";
  private static final String BACKGROUND_IMAGE = "background_image";
  private static final String BACKGROUND_IMAGE_LARGE = "background_image_large";
  private static final String MENU_URL = "menu_url";
  private static final String BLACKLIST_LOCATION_NAMES = "blacklist_location_names";
  private static final String PHONETIC_MARKUP = "phonetic_markup";
  private static final String PHONETIC_ALIASES = "phonetic_aliases";
  private static final String POST_AT_NEW_LOCATION = "post_at_new_location";
  private static final String TWITTER_TOKEN = "twitter_token";
  private static final String TWITTER_TOKEN_SECRET = "twitter_token_secret";
  private static final String POST_WEEKLY_SCHEDULE = "post_weekly_schedule";
  private static final String POST_DAILY_SCHEDULE = "post_daily_schedule";
  private static final String NEVER_LINK_TWITTER = "never_link_twitter";
  private static final String FACEBOOK_ACCESS_TOKEN = "facebook_access_token";
  private static final String FACEBOOK_ACCESS_TOKEN_EXPIRY = "facebook_access_token_expiry";
  private static final String NOTIFY_OF_LOCATION_CHANGES = "notify_of_location_changes";
  private static final String NOTIFY_WHEN_LEAVING = "notify_when_leaving";
  private static final String NOTIFY_WHEN_DEVICE_ISSUES = "notify_when_device_issues";
  private static final String DRUPAL_CALENDAR = "drupal_calendar";
  private static final String ICAL_CALENDAR = "ical_calendar";
  private static final String SQUARESPACE_CALENDAR = "squarespace_calendar";
  private DateTimeZone zone;

  @Inject
  public TruckDAOAppEngine(Provider<DatastoreService> provider, DateTimeZone zone) {
    super(TRUCK_KIND, provider);
    this.zone = zone;
  }

  protected Truck fromEntity(Entity entity) {
    Truck.Builder builder = Truck.builder();
    Collection categoriesList = (Collection) entity.getProperty(CATEGORIES_FIELD);
    Text t = (Text) entity.getProperty(TRUCK_DESCRIPTION_FIELD);
    Truck.Stats stats = null;
    if (entity.hasProperty(TRUCK_STATS_TOTAL_STOPS)) {
      Location lastSeenLocation = null, firstSeenLocation = null;
      if (entity.hasProperty(TRUCK_STATS_LAST_SEEN_WHERE)) {
        lastSeenLocation = Location.builder()
            .lat(getDoubleProperty(entity, TRUCK_STATS_LAST_SEEN_WHERE_LAT, 0))
            .lng(getDoubleProperty(entity, TRUCK_STATS_LAST_SEEN_WHERE_LNG, 0))
            .name(getStringProperty(entity, TRUCK_STATS_LAST_SEEN_WHERE))
            .build();
      }
      if (entity.hasProperty(TRUCK_STATS_FIRST_SEEN_WHERE)) {
        firstSeenLocation = Location.builder()
            .lat(getDoubleProperty(entity, TRUCK_STATS_FIRST_SEEN_WHERE_LAT, 0))
            .lng(getDoubleProperty(entity, TRUCK_STATS_FIRST_SEEN_WHERE_LNG, 0))
            .name(getStringProperty(entity, TRUCK_STATS_FIRST_SEEN_WHERE))
            .build();
      }
      stats = Truck.Stats.builder()
          .firstSeen(getDateTime(entity, TRUCK_STATS_FIRST_SEEN_WHEN, zone))
          .whereFirstSeen(firstSeenLocation)
          .lastSeen(getDateTime(entity, TRUCK_STATS_LAST_SEEN_WHEN, zone))
          .lastUpdate(getDateTime(entity, TRUCK_STATS_LAST_UPDATED, zone))
          .stopsThisYear(getLongProperty(entity, TRUCK_STATS_STOPS_THIS_YEAR, 0))
          .totalStops(getLongProperty(entity, TRUCK_STATS_TOTAL_STOPS, 0))
          .whereLastSeen(lastSeenLocation)
          .build();
    }

    //noinspection unchecked
    return builder.id(entity.getKey()
        .getName())
        .stats(stats)
        .fullsizeImage(getStringProperty(entity, TRUCK_FULL_SIZE))
        .displayEmailPublicly(getBooleanProperty(entity, TRUCK_DISPLAY_EMAIL, true))
        .previewIcon(getStringProperty(entity, TRUCK_PREVIEW_ICON))
        .inactive((Boolean) entity.getProperty(INACTIVE_FIELD))
        .twitterHandle((String) entity.getProperty(TRUCK_TWITTER_HANDLE))
        .defaultCity((String) entity.getProperty(TRUCK_DEFAULT_CITY_FIELD))
        .description(t == null ? null : t.getValue())
        .hidden(getBooleanProperty(entity, TRUCK_HIDDEN, false))
        .instagramId(getStringProperty(entity, TRUCK_INSTAGRAM))
        .facebook((String) entity.getProperty(TRUCK_FACEBOOK_FIELD))
        .scanFacebook(getBooleanProperty(entity, SCAN_FACEBOOK, false))
        .notifyOfLocationChanges(getBooleanProperty(entity, NOTIFY_OF_LOCATION_CHANGES, false))
        .lastScanned(getStringProperty(entity, LAST_SCANNED))
        .foursquareUrl((String) entity.getProperty(TRUCK_FOURSQUARE_URL_FIELD))
        .iconUrl((String) entity.getProperty(TRUCK_ICON_URL))
        .menuUrl(getStringProperty(entity, MENU_URL))
        .fleetSize(getIntProperty(entity, FLEET_SIZE, 1))
        .backgroundImage(getStringProperty(entity, BACKGROUND_IMAGE, null))
        .backgroundImageLarge(getStringProperty(entity, BACKGROUND_IMAGE_LARGE, null))
        .muteUntil(Attributes.getDateTime(entity, TRUCK_MUTE_UNTIL, zone))
        .name((String) entity.getProperty(TRUCK_NAME_FIELD))
        .yelpSlug(getStringProperty(entity, TRUCK_YELP_SLUG))
        .facebookPageId(getStringProperty(entity, TRUCK_FACEBOOK_PAGE_ID))
        .matchOnlyIf((String) entity.getProperty(MATCH_REGEX_FIELD))
        .donotMatchIf((String) entity.getProperty(DONT_MATCH_REGEX_FIELD))
        .beaconnaiseEmails(getSetProperty(entity, TRUCK_BEACONNAISE_EMAILS))
        .timezoneOffset(getIntProperty(entity, TIMEZONE_OFFSET, 0))
        .url((String) entity.getProperty(TRUCK_URL))
        .phoneticMarkup(Strings.emptyToNull((String) entity.getProperty(PHONETIC_MARKUP)))
        .blacklistLocationNames(getListProperty(entity, BLACKLIST_LOCATION_NAMES))
        .phoneticAliases(getListProperty(entity, PHONETIC_ALIASES))
        .categories(categoriesList == null ? ImmutableSet.of() : ImmutableSet.copyOf(categoriesList))
        .useTwittalyzer((Boolean) entity.getProperty(TRUCK_TWITTALYZER_FIELD))
        .calendarUrl((String) entity.getProperty(TRUCK_CALENDAR_URL))
        .phone((String) entity.getProperty(TRUCK_PHONE))
        .email((String) entity.getProperty(TRUCK_EMAIL))
        .postWeeklySchedule(getBooleanProperty(entity, POST_WEEKLY_SCHEDULE, false))
        .postDailySchedule(getBooleanProperty(entity, POST_DAILY_SCHEDULE, false))
        .postAtNewStop(getBooleanProperty(entity, POST_AT_NEW_LOCATION, false))
        .twitterToken(getStringProperty(entity, TWITTER_TOKEN))
        .twitterTokenSecret(getStringProperty(entity, TWITTER_TOKEN_SECRET))
        .neverLinkTwitter(getBooleanProperty(entity, NEVER_LINK_TWITTER, false))
        .facebookAccessToken(getStringProperty(entity, FACEBOOK_ACCESS_TOKEN, null))
        .facebookAccessTokenExpires(getDateTime(entity, FACEBOOK_ACCESS_TOKEN_EXPIRY, zone))
        .notifyWhenLeaving(getBooleanProperty(entity, NOTIFY_WHEN_LEAVING, false))
        .notifyWhenDeviceIssues(getBooleanProperty(entity, NOTIFY_WHEN_DEVICE_ISSUES, false))
        .drupalCalendar(getStringProperty(entity, DRUPAL_CALENDAR))
        .icalCalendar(getStringProperty(entity, ICAL_CALENDAR))
        .squarespaceCalendar(getStringProperty(entity, SQUARESPACE_CALENDAR))
        .build();
  }

  @Override
  protected void modifyFindAllQuery(Query q) {
    q.addSort(TRUCK_CANONICAL_NAME);
  }

  @Override
  public Collection<Truck> findByTwitterId(String screenName) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.setFilter(new Query.FilterPredicate(TRUCK_TWITTER_HANDLE, EQUAL, screenName));
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q)
        .asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  @Override
  public Collection<Truck> findInactiveTrucks() {
    return executeQuery(new Query(getKind()).addSort(TRUCK_CANONICAL_NAME)
        .setFilter(new Query.FilterPredicate(INACTIVE_FIELD, EQUAL, true)));
  }

  @Override
  public List<Truck> findActiveTrucks() {
    return executeQuery(new Query(getKind()).addSort(TRUCK_CANONICAL_NAME)
        .setFilter(new Query.FilterPredicate(INACTIVE_FIELD, EQUAL, false)));
  }

  public List<Truck> findVisibleTrucks() {
    return executeQuery(new Query(TRUCK_KIND).setFilter(new Query.FilterPredicate(TRUCK_HIDDEN, EQUAL, false))
        .addSort(TRUCK_CANONICAL_NAME));
  }

  @Override
  public List<Truck> findFacebookTrucks() {
    return executeQuery(new Query(TRUCK_KIND).setFilter(new Query.FilterPredicate(SCAN_FACEBOOK, EQUAL, true))
        .addSort(TRUCK_CANONICAL_NAME));
  }

  @Nullable
  @Override
  public Truck findFirst() {
    return Iterables.getFirst(findAll(), null);
  }

  @Override
  public List<Truck> findByCategory(String tag) {
    ImmutableList.Builder<Truck> trucks = ImmutableList.builder();
    for (Truck truck : findVisibleTrucks()) {
      if (truck.getCategories()
          .contains(tag)) {
        trucks.add(truck);
      }
    }
    return trucks.build();
  }

  @Override
  public Set<Truck> findByBeaconnaiseEmail(String email) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.setFilter(new Query.FilterPredicate(TRUCK_BEACONNAISE_EMAILS, Query.FilterOperator.IN, ImmutableSet.of(email)));
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q)
        .asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  @Override
  public Iterable<Truck> findTrucksWithEmail() {
    Query q = new Query(TRUCK_KIND);
    q.setFilter(
        Query.CompositeFilterOperator.and(new Query.FilterPredicate(TRUCK_EMAIL, Query.FilterOperator.NOT_EQUAL, null),
            new Query.FilterPredicate(TRUCK_ALLOW_SYSTEM_NOTIFICATIONS, EQUAL, true)));
    return executeQuery(q);
  }

  @Override
  public void deleteAll() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(getKind());
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q)
        .asIterable()) {
      keys.add(entity.getKey());
    }
    dataStore.delete(keys.build());
  }

  @Nullable
  @Override
  public Truck findByName(String name) {
    return aq().filter(predicate(TRUCK_CANONICAL_NAME, EQUAL, Truck.canonize(name)))
        .findFirst();
  }

  @Nullable
  @Override
  public Truck findByNameOrAlias(String name) {
    Truck t = findByName(name);
    if (t == null) {
      return aq().filter(predicate(PHONETIC_ALIASES, EQUAL, name.toLowerCase()))
          .findFirst();
    }
    return t;
  }

  @Override
  public Set<Truck> findTrucksWithCalendars() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.setFilter(new Query.FilterPredicate(TRUCK_CALENDAR_URL, Query.FilterOperator.NOT_EQUAL, null));
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q)
        .asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  @Override
  public Set<Truck> findTruckWithDrupalCalendars() {
    Query q = new Query(TRUCK_KIND);
    q.setFilter(new Query.FilterPredicate(DRUPAL_CALENDAR, Query.FilterOperator.NOT_EQUAL, null));
    return ImmutableSet.copyOf(executeQuery(q));
  }

  @Override
  public Set<Truck> findTruckWithICalCalendars() {
    Query q = new Query(TRUCK_KIND);
    q.setFilter(new Query.FilterPredicate(ICAL_CALENDAR, Query.FilterOperator.NOT_EQUAL, null));
    return ImmutableSet.copyOf(executeQuery(q));
  }

  @Override
  public Set<Truck> findTruckWithSquarespaceCalendars() {
    Query q = new Query(TRUCK_KIND);
    q.setFilter(new Query.FilterPredicate(SQUARESPACE_CALENDAR, Query.FilterOperator.NOT_EQUAL, null));
    return ImmutableSet.copyOf(executeQuery(q));
  }

  protected Entity toEntity(Truck truck, Entity entity) {
    entity.setProperty(TRUCK_CANONICAL_NAME, truck.canonicalName());
    entity.setProperty(TRUCK_DISPLAY_EMAIL, truck.isDisplayEmailPublicly());
    entity.setProperty(TRUCK_NAME_FIELD, truck.getName());
    entity.setProperty(NOTIFY_OF_LOCATION_CHANGES, truck.isNotifyOfLocationChanges());
    entity.setProperty(TRUCK_TWITTER_HANDLE, truck.getTwitterHandle());
    entity.setProperty(TRUCK_URL, truck.getUrl());
    entity.setProperty(TRUCK_PREVIEW_ICON, truck.getPreviewIcon());
    entity.setProperty(TRUCK_ICON_URL, truck.getIconUrl());
    entity.setProperty(TRUCK_BEACONNAISE_EMAILS, truck.getBeaconnaiseEmails());
    entity.setProperty(TRUCK_CALENDAR_URL, Strings.emptyToNull(truck.getCalendarUrl()));
    entity.setProperty(TRUCK_DESCRIPTION_FIELD, new Text(Strings.nullToEmpty(truck.getDescription())));
    entity.setProperty(TRUCK_FOURSQUARE_URL_FIELD, truck.getFoursquareUrl());
    entity.setProperty(TRUCK_TWITTALYZER_FIELD, truck.isUsingTwittalyzer());
    entity.setProperty(TRUCK_DEFAULT_CITY_FIELD, truck.getDefaultCity());
    entity.setProperty(TRUCK_FACEBOOK_FIELD, truck.getFacebook());
    entity.setProperty(MATCH_REGEX_FIELD, truck.getMatchOnlyIfString());
    entity.setProperty(DONT_MATCH_REGEX_FIELD, truck.getDonotMatchIfString());
    entity.setProperty(INACTIVE_FIELD, truck.isInactive());
    entity.setProperty(TIMEZONE_OFFSET, truck.getTimezoneAdjustment());
    entity.setProperty(CATEGORIES_FIELD, truck.getCategories());
    entity.setProperty(TRUCK_EMAIL, truck.getEmail());
    entity.setProperty(TRUCK_YELP_SLUG, truck.getYelpSlug());
    entity.setProperty(FLEET_SIZE, truck.getFleetSize());
    entity.setProperty(BACKGROUND_IMAGE, truck.getBackgroundImage());
    entity.setProperty(BACKGROUND_IMAGE_LARGE, truck.getBackgroundImageLarge());
    entity.setProperty(MENU_URL, truck.getMenuUrl());
    entity.setProperty(TRUCK_PHONE, truck.getPhone());
    entity.setProperty(PHONETIC_MARKUP, Strings.emptyToNull(truck.getPhoneticMarkup()));
    entity.setProperty(TRUCK_HIDDEN, truck.isHidden());
    entity.setProperty(TRUCK_INSTAGRAM, truck.getInstagramId());
    entity.setProperty(TRUCK_FULL_SIZE, truck.getFullsizeImage());
    entity.setProperty(SCAN_FACEBOOK, truck.getScanFacebook());
    entity.setProperty(LAST_SCANNED, truck.getLastScanned());
    entity.setProperty(TRUCK_FACEBOOK_PAGE_ID, truck.getFacebookPageId());
    entity.setProperty(BLACKLIST_LOCATION_NAMES, truck.getBlacklistLocationNames());
    entity.setProperty(PHONETIC_ALIASES, truck.getPhoneticAliases());
    entity.setProperty(DRUPAL_CALENDAR, truck.getDrupalCalendar());
    entity.setProperty(ICAL_CALENDAR, Strings.emptyToNull(truck.getIcalCalendar()));
    entity.setProperty(SQUARESPACE_CALENDAR, Strings.emptyToNull(truck.getSquarespaceCalendar()));
    entity.setProperty(TWITTER_TOKEN, truck.getTwitterToken());
    entity.setProperty(TWITTER_TOKEN_SECRET, truck.getTwitterTokenSecret());
    entity.setProperty(POST_AT_NEW_LOCATION, truck.isPostAtNewStop());
    entity.setProperty(POST_WEEKLY_SCHEDULE, truck.isPostWeeklySchedule());
    entity.setProperty(POST_DAILY_SCHEDULE, truck.isPostDailySchedule());
    setDateProperty(FACEBOOK_ACCESS_TOKEN_EXPIRY, entity, truck.getFacebookAccessTokenExpires());
    entity.setProperty(FACEBOOK_ACCESS_TOKEN, truck.getFacebookAccessToken());
    Attributes.setDateProperty(TRUCK_MUTE_UNTIL, entity, truck.getMuteUntil());
    entity.setProperty(NEVER_LINK_TWITTER, truck.isNeverLinkTwitter());
    entity.setProperty(NOTIFY_WHEN_LEAVING, truck.isNotifyWhenLeaving());
    entity.setProperty(NOTIFY_WHEN_DEVICE_ISSUES, truck.isNotifyWhenDeviceIssues());
    Truck.Stats stats = truck.getStats();
    if (stats == null) {
      stats = Truck.Stats.builder()
          .build();
    }
    setDateProperty(TRUCK_STATS_LAST_SEEN_WHEN, entity, stats.getLastSeen());
    setDateProperty(TRUCK_STATS_FIRST_SEEN_WHEN, entity, stats.getFirstSeen());
    Location whereLastSeen = stats.getWhereLastSeen(), whereFirstSeen = stats.getWhereFirstSeen();
    if (whereLastSeen != null) {
      entity.setProperty(TRUCK_STATS_LAST_SEEN_WHERE, whereLastSeen.getName());
      entity.setProperty(TRUCK_STATS_LAST_SEEN_WHERE_LAT, whereLastSeen.getLatitude());
      entity.setProperty(TRUCK_STATS_LAST_SEEN_WHERE_LNG, whereLastSeen.getLongitude());
    }
    if (whereFirstSeen != null) {
      entity.setProperty(TRUCK_STATS_FIRST_SEEN_WHERE, whereFirstSeen.getName());
      entity.setProperty(TRUCK_STATS_FIRST_SEEN_WHERE_LAT, whereFirstSeen.getLatitude());
      entity.setProperty(TRUCK_STATS_FIRST_SEEN_WHERE_LNG, whereFirstSeen.getLongitude());
    }
    entity.setProperty(TRUCK_STATS_LAST_UPDATED, stats.getLastUpdated()
        .toDate());
    entity.setProperty(TRUCK_STATS_TOTAL_STOPS, stats.getTotalStops());
    entity.setProperty(TRUCK_STATS_STOPS_THIS_YEAR, stats.getStopsThisYear());
    return entity;
  }
}
