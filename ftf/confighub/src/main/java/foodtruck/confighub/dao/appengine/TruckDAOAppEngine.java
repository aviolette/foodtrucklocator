package foodtruck.confighub.dao.appengine;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.cloud.datastore.BaseEntity;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.ListValue;
import com.google.cloud.datastore.NullValue;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.Value;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import autovalue.shaded.com.google.common.common.primitives.Ints;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;

import static com.google.cloud.datastore.StructuredQuery.PropertyFilter.eq;


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
  private static final String TRUCK_TWITTER_GEOLOCATION = "twitterGeolocation";
  private static final String TRUCK_MUTE_UNTIL = "muteUntil";
  private static final String TRUCK_YELP_SLUG = "yelp";
  private static final String TRUCK_FACEBOOK_PAGE_ID = "facebookPageId";
  private static final String TRUCK_STATS_LAST_SEEN_WHEN = "last_seen_when";
  private static final String TRUCK_STATS_LAST_SEEN_WHERE = "last_seen_where";
  private static final String TRUCK_STATS_LAST_UPDATED = "last_updated";
  private static final String TRUCK_STATS_TOTAL_STOPS = "total_stops";
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
  private DateTimeZone zone;

  @Inject
  public TruckDAOAppEngine(Datastore datastore, DateTimeZone zone) {
    super(TRUCK_KIND, datastore);
    this.zone = zone;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void toEntity(Truck truck, BaseEntity.Builder entity) {
    entity.set(TRUCK_CANONICAL_NAME, truck.canonicalName())
        .set(TRUCK_DISPLAY_EMAIL, truck.isDisplayEmailPublicly())
        .set(TRUCK_NAME_FIELD, truck.getName())
        .set(TRUCK_TWITTER_HANDLE, truck.getTwitterHandle())
        .set(TRUCK_URL, truck.getUrl())
        .set(TRUCK_PREVIEW_ICON, truck.getPreviewIcon())
        .set(TRUCK_ICON_URL, truck.getIconUrl())
        .set(TRUCK_BEACONNAISE_EMAILS, toList(truck.getBeaconnaiseEmails()))
        .set(TRUCK_DESCRIPTION_FIELD, Strings.nullToEmpty(truck.getDescription()))
        .set(TRUCK_FOURSQUARE_URL_FIELD, truck.getFoursquareUrl())
        .set(TRUCK_TWITTALYZER_FIELD, truck.isUsingTwittalyzer())
        .set(TRUCK_DEFAULT_CITY_FIELD, truck.getDefaultCity())
        .set(TRUCK_FACEBOOK_FIELD, truck.getFacebook())
        .set(INACTIVE_FIELD, truck.isInactive())
        .set(TIMEZONE_OFFSET, truck.getTimezoneAdjustment())
        .set(CATEGORIES_FIELD, toList(truck.getCategories()))
        .set(TRUCK_YELP_SLUG, truck.getYelpSlug())
        .set(FLEET_SIZE, truck.getFleetSize())
        .set(TRUCK_HIDDEN, truck.isHidden())
        .set(SCAN_FACEBOOK, truck.getScanFacebook())
        .set(TRUCK_FACEBOOK_PAGE_ID, truck.getFacebookPageId())
        .set(BLACKLIST_LOCATION_NAMES, toList(truck.getBlacklistLocationNames()))
        .set(PHONETIC_ALIASES, toList(truck.getPhoneticAliases()));
    setString(entity, LAST_SCANNED, truck.getLastScanned());
    setString(entity, PHONETIC_MARKUP, Strings.emptyToNull(truck.getPhoneticMarkup()));
    setString(entity, TRUCK_FULL_SIZE, truck.getFullsizeImage());
    setString(entity, BACKGROUND_IMAGE, truck.getBackgroundImage());
    setString(entity, BACKGROUND_IMAGE_LARGE, truck.getBackgroundImageLarge());
    setString(entity, TRUCK_FACEBOOK_PAGE_ID, truck.getFacebookPageId());
    setString(entity, TRUCK_INSTAGRAM, truck.getInstagramId());
    setString(entity, TRUCK_PHONE, truck.getPhone());
    setString(entity, MENU_URL, truck.getMenuUrl());
    setString(entity, TRUCK_EMAIL, truck.getEmail());
    setString(entity, DONT_MATCH_REGEX_FIELD, truck.getDonotMatchIfString());
    setString(entity, MATCH_REGEX_FIELD, truck.getMatchOnlyIfString());
    setString(entity, TRUCK_CALENDAR_URL, truck.getCalendarUrl());
    setDateProperty(TRUCK_MUTE_UNTIL, entity, truck.getMuteUntil());
    Truck.Stats stats = truck.getStats();
    if (stats == null) {
      stats = Truck.Stats.builder()
          .build();
    }
    setDateProperty(TRUCK_STATS_LAST_SEEN_WHEN, entity, stats.getLastSeen());
    setDateProperty(TRUCK_STATS_FIRST_SEEN_WHEN, entity, stats.getFirstSeen());
    Location whereLastSeen = stats.getWhereLastSeen(), whereFirstSeen = stats.getWhereFirstSeen();
    if (whereLastSeen != null) {
      entity.set(TRUCK_STATS_LAST_SEEN_WHERE, whereLastSeen.getName())
          .set(TRUCK_STATS_LAST_SEEN_WHERE_LAT, whereLastSeen.getLatitude())
          .set(TRUCK_STATS_LAST_SEEN_WHERE_LNG, whereLastSeen.getLongitude());
    }
    if (whereFirstSeen != null) {
      entity.set(TRUCK_STATS_FIRST_SEEN_WHERE, whereFirstSeen.getName())
          .set(TRUCK_STATS_FIRST_SEEN_WHERE_LAT, whereFirstSeen.getLatitude())
          .set(TRUCK_STATS_FIRST_SEEN_WHERE_LNG, whereFirstSeen.getLongitude());
    }
    setDateProperty(TRUCK_STATS_LAST_UPDATED, entity, stats.getLastUpdated());

    entity.set(TRUCK_STATS_TOTAL_STOPS, stats.getTotalStops())
        .set(TRUCK_STATS_STOPS_THIS_YEAR, stats.getStopsThisYear());
  }

  protected IncompleteKey makeKey(Truck obj) {
    return keyFactory.newKey(obj.getId());
  }


  private void setString(BaseEntity.Builder entity, String name, String value) {
    if (value == null) {
      entity.set(name, NullValue.of());
    } else {
      entity.set(name, value);
    }
  }

  private Value<?> toList(Collection<String> values) {
    ListValue.Builder lvs = ListValue.builder();
    if (values.isEmpty()) {
      return NullValue.of();
    }
    for (String s : values) {
      lvs.addValue(StringValue.of(s));
    }
    return lvs.build();
  }

  protected Truck fromEntity(Entity entity) {
    Truck.Builder builder = Truck.builder();
    Truck.Stats stats = null;
    if (entity.contains(TRUCK_STATS_TOTAL_STOPS)) {
      Location lastSeenLocation = null, firstSeenLocation = null;
      if (entity.contains(TRUCK_STATS_LAST_SEEN_WHERE)) {
        lastSeenLocation = Location.builder()
            .lat(entity.getDouble(TRUCK_STATS_LAST_SEEN_WHERE_LAT))
            .lng(entity.getDouble(TRUCK_STATS_LAST_SEEN_WHERE_LNG))
            .name(getString(entity, (TRUCK_STATS_LAST_SEEN_WHERE)))
            .build();
      }
      if (entity.contains(TRUCK_STATS_FIRST_SEEN_WHERE)) {
        firstSeenLocation = Location.builder()
            .lat(entity.getDouble(TRUCK_STATS_FIRST_SEEN_WHERE_LAT))
            .lng(entity.getDouble(TRUCK_STATS_FIRST_SEEN_WHERE_LNG))
            .name(getString(entity, (TRUCK_STATS_FIRST_SEEN_WHERE)))
            .build();
      }
      stats = Truck.Stats.builder()
          .firstSeen(getDateTime(entity, TRUCK_STATS_FIRST_SEEN_WHEN, zone))
          .whereFirstSeen(firstSeenLocation)
          .lastSeen(getDateTime(entity, TRUCK_STATS_LAST_SEEN_WHEN, zone))
          .lastUpdate(getDateTime(entity, TRUCK_STATS_LAST_UPDATED, zone))
          .stopsThisYear(getLong(entity, TRUCK_STATS_STOPS_THIS_YEAR))
          .totalStops(getLong(entity, TRUCK_STATS_TOTAL_STOPS))
          .whereLastSeen(lastSeenLocation)
          .build();
    }
    //noinspection unchecked
    builder.id(entity.key().name())
        .stats(stats)
        .description(getString(entity, TRUCK_DESCRIPTION_FIELD))
        .fullsizeImage(getString(entity, TRUCK_FULL_SIZE))
        .displayEmailPublicly(getBoolean(entity, TRUCK_DISPLAY_EMAIL))
        .allowSystemNotifications(getBoolean(entity, TRUCK_ALLOW_SYSTEM_NOTIFICATIONS))
        .previewIcon(getString(entity, TRUCK_PREVIEW_ICON))
        .inactive(getBoolean(entity, INACTIVE_FIELD))
        .twitterHandle(getString(entity, TRUCK_TWITTER_HANDLE))
        .defaultCity(getString(entity, TRUCK_DEFAULT_CITY_FIELD))
        .description(getString(entity, TRUCK_DESCRIPTION_FIELD))
        .hidden(getBoolean(entity, TRUCK_HIDDEN))
        .instagramId(getString(entity, TRUCK_INSTAGRAM))
        .facebook(getString(entity, TRUCK_FACEBOOK_FIELD))
        .scanFacebook(getBoolean(entity, SCAN_FACEBOOK))
        .lastScanned(getString(entity, LAST_SCANNED))
        .foursquareUrl(getString(entity, TRUCK_FOURSQUARE_URL_FIELD))
        .iconUrl(getString(entity, TRUCK_ICON_URL))
        .menuUrl(getString(entity, MENU_URL))
        .fleetSize(Ints.checkedCast(getLong(entity, FLEET_SIZE)))
        .backgroundImage(getString(entity, BACKGROUND_IMAGE))
        .backgroundImageLarge(getString(entity, BACKGROUND_IMAGE_LARGE))
        .muteUntil(getDateTime(entity, TRUCK_MUTE_UNTIL, zone))
        .name(getString(entity, TRUCK_NAME_FIELD))
        .yelpSlug(getString(entity, TRUCK_YELP_SLUG))
        .facebookPageId(getString(entity, TRUCK_FACEBOOK_PAGE_ID))
        .matchOnlyIf(getString(entity, MATCH_REGEX_FIELD))
        .donotMatchIf(getString(entity, DONT_MATCH_REGEX_FIELD));
    return builder
        .beaconnaiseEmails(getSet(TRUCK_BEACONNAISE_EMAILS, entity))
        .timezoneOffset(Ints.checkedCast(getLong(entity, TIMEZONE_OFFSET)))
        .url(getString(entity, TRUCK_URL))
        .phoneticMarkup(Strings.emptyToNull(getString(entity, PHONETIC_MARKUP)))
        .blacklistLocationNames(getList(BLACKLIST_LOCATION_NAMES, entity))
        .phoneticAliases(getList(PHONETIC_ALIASES, entity))
        .categories(getSet(CATEGORIES_FIELD, entity))
        .useTwittalyzer(getBoolean(entity, TRUCK_TWITTALYZER_FIELD))
        .twitterGeolocationDataValid(getBoolean(entity, TRUCK_TWITTER_GEOLOCATION))
        .calendarUrl(getString(entity, TRUCK_CALENDAR_URL))
        .phone(getString(entity, TRUCK_PHONE))
        .email(getString(entity, TRUCK_EMAIL))
        .build();
  }

  private DateTime getDateTime(Entity entity, String propertyName, DateTimeZone zone) {
    try {
      com.google.cloud.datastore.DateTime date = entity.getDateTime(propertyName);
      if (date == null) {
        return null;
      }
      return new DateTime(date.toDate(), zone);
    } catch (DatastoreException dse) {
      return null;
    }
  }

  @Override
  public Collection<Truck> findByTwitterId(String twitter) {
    return executeQuery(Query.entityQueryBuilder()
        .kind(getKind())
        .filter(eq(TRUCK_TWITTER_HANDLE, twitter))
        .build(), null);
  }

  @Override
  public List<Truck> findActiveTrucks() {
    return executeQuery(Query.entityQueryBuilder()
        .kind(getKind())
        .orderBy(StructuredQuery.OrderBy.asc(TRUCK_CANONICAL_NAME))
        .filter(eq(INACTIVE_FIELD, false))
        .build(), null);
  }

  @Override
  public List<Truck> findInactiveTrucks() {
    return executeQuery(Query.entityQueryBuilder()
        .kind(getKind())
        .orderBy(StructuredQuery.OrderBy.asc(TRUCK_CANONICAL_NAME))
        .filter(eq(INACTIVE_FIELD, true))
        .build(), null);
  }

  @Override
  public Collection<Truck> findTrucksWithCalendars() {
    return executeQuery(Query.entityQueryBuilder()
        .kind(getKind())
        .filter(eq(INACTIVE_FIELD, false))
        .build(), e -> !Strings.isNullOrEmpty(e.getString(TRUCK_CALENDAR_URL)));
  }

  @Override
  public List<Truck> findVisibleTrucks() {
    return executeQuery(Query.entityQueryBuilder()
        .kind(getKind())
        .filter(eq(TRUCK_HIDDEN, false))
        .build(), null);
  }

  @Override
  public List<Truck> findFacebookTrucks() {
    return executeQuery(Query.entityQueryBuilder()
        .kind(getKind())
        .filter(eq(SCAN_FACEBOOK, true))
        .build(), null);
  }

  @Nullable
  @Override
  public Truck findFirst() {
    return Iterables.getFirst(executeQuery(Query.entityQueryBuilder()
        .kind(getKind())
        .limit(1)
        .build(), null), null);
  }

  @Override
  public List<Truck> findByCategory(String category) {
    return executeQuery(Query.entityQueryBuilder()
        .kind(getKind())
        .filter(eq(CATEGORIES_FIELD, category))
        .build(), null);
  }

  @Override
  public Collection<Truck> findByBeaconnaiseEmail(String email) {
    return executeQuery(Query.entityQueryBuilder()
        .kind(getKind())
        .build(), e -> getList(TRUCK_BEACONNAISE_EMAILS, e).contains(email));

  }

  @Override
  public Iterable<Truck> findTrucksWithEmail() {
    return executeQuery(Query.entityQueryBuilder()
        .kind(getKind())
        .filter(eq(TRUCK_ALLOW_SYSTEM_NOTIFICATIONS, true))
        .build(), e -> !Strings.isNullOrEmpty(e.getString(TRUCK_EMAIL)));
  }

  @Override
  public void deleteAll() {

  }

  @Nullable
  @Override
  public Truck findByName(String name) {
    return Iterables.getFirst(executeQuery(Query.entityQueryBuilder()
        .kind(getKind())
        .limit(1)
        .filter(eq(TRUCK_CANONICAL_NAME, Truck.canonize(name)))
        .build(), null), null);
  }

  @Nullable
  @Override
  public Truck findByNameOrAlias(String s) {
    return null;
  }
}
