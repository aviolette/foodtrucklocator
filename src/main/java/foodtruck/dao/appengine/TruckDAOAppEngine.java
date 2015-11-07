package foodtruck.dao.appengine;

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

import org.joda.time.DateTimeZone;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;

import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.dao.appengine.Attributes.getIntProperty;
import static foodtruck.dao.appengine.Attributes.getLongProperty;
import static foodtruck.dao.appengine.Attributes.getSetProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;
import static foodtruck.dao.appengine.Attributes.setDateProperty;

/**
 * @author aviolette@gmail.com
 * @since 2/26/12
 */
public class TruckDAOAppEngine extends AppEngineDAO<String, Truck> implements TruckDAO {
  private static final String TRUCK_KIND = "Store";
  private static final String TRUCK_NAME_FIELD = "name";
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

  private DateTimeZone zone;

  @Inject
  public TruckDAOAppEngine(DatastoreServiceProvider provider, DateTimeZone zone) {
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

    return builder.id(entity.getKey().getName())
        .stats(stats)
        .fullsizeImage(getStringProperty(entity, TRUCK_FULL_SIZE))
        .displayEmailPublicly(getBooleanProperty(entity, TRUCK_DISPLAY_EMAIL, true))
        .allowSystemNotifications(getBooleanProperty(entity, TRUCK_ALLOW_SYSTEM_NOTIFICATIONS, false))
        .previewIcon(getStringProperty(entity, TRUCK_PREVIEW_ICON))
        .inactive((Boolean) entity.getProperty(INACTIVE_FIELD))
        .twitterHandle((String) entity.getProperty(TRUCK_TWITTER_HANDLE))
        .defaultCity((String) entity.getProperty(TRUCK_DEFAULT_CITY_FIELD))
        .description(t == null ? null : t.getValue())
        .hidden(getBooleanProperty(entity, TRUCK_HIDDEN, false))
        .instagramId(getStringProperty(entity, TRUCK_INSTAGRAM))
        .facebook((String) entity.getProperty(TRUCK_FACEBOOK_FIELD))
        .scanFacebook(getBooleanProperty(entity, SCAN_FACEBOOK, false))
        .lastScanned(getStringProperty(entity, LAST_SCANNED))
        .foursquareUrl((String) entity.getProperty(TRUCK_FOURSQUARE_URL_FIELD))
        .iconUrl((String) entity.getProperty(TRUCK_ICON_URL))
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
        .categories(categoriesList == null ? ImmutableSet.<String>of() :
            ImmutableSet.copyOf(categoriesList))
        .useTwittalyzer((Boolean) entity.getProperty(TRUCK_TWITTALYZER_FIELD))
        .twitterGeolocationDataValid(getBooleanProperty(entity, TRUCK_TWITTER_GEOLOCATION, false))
        .calendarUrl((String) entity.getProperty(TRUCK_CALENDAR_URL))
        .phone((String) entity.getProperty(TRUCK_PHONE))
        .email((String) entity.getProperty(TRUCK_EMAIL))
        .build();
  }

  @Override protected void modifyFindAllQuery(Query q) {
    q.addSort(TRUCK_NAME_FIELD);
  }

  @Override public Collection<Truck> findByTwitterId(String screenName) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.setFilter(new Query.FilterPredicate(TRUCK_TWITTER_HANDLE, Query.FilterOperator.EQUAL, screenName));
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  @Override public Collection<Truck> findInactiveTrucks() {
    DatastoreService dataStore = provider.get();
    return executeQuery(dataStore,
        new Query(getKind())
            .addSort(TRUCK_NAME_FIELD)
            .setFilter(new Query.FilterPredicate(INACTIVE_FIELD, Query.FilterOperator.EQUAL, true)), null);
  }

  @Override public Collection<Truck> findActiveTrucks() {
    DatastoreService dataStore = provider.get();
    return executeQuery(dataStore,
        new Query(getKind())
          .addSort(TRUCK_NAME_FIELD)
          .setFilter(new Query.FilterPredicate(INACTIVE_FIELD, Query.FilterOperator.EQUAL, false)), null);
  }

  public List<Truck> findVisibleTrucks() {
    DatastoreService dataStore = provider.get();
    return executeQuery(dataStore,
        new Query(TRUCK_KIND)
            .setFilter(new Query.FilterPredicate(TRUCK_HIDDEN, Query.FilterOperator.EQUAL, false))
            .addSort(TRUCK_NAME_FIELD), null);
  }

  @Override
  public List<Truck> findFacebookTrucks() {
    DatastoreService dataStore = provider.get();
    return executeQuery(dataStore,
        new Query(TRUCK_KIND)
            .setFilter(new Query.FilterPredicate(SCAN_FACEBOOK, Query.FilterOperator.EQUAL, true))
            .addSort(TRUCK_NAME_FIELD), null);
  }

  @Nullable @Override public Truck findFirst() {
    return Iterables.getFirst(findAll(), null);
  }

  @Override public Collection<Truck> findByCategory(String tag) {
    ImmutableList.Builder<Truck> trucks = ImmutableList.builder();
    for (Truck truck : findVisibleTrucks()) {
      if (truck.getCategories().contains(tag)) {
        trucks.add(truck);
      }
    }
    return trucks.build();
  }

  @Override public Set<Truck> findByBeaconnaiseEmail(String email) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.setFilter(new Query.FilterPredicate(TRUCK_BEACONNAISE_EMAILS, Query.FilterOperator.IN, ImmutableSet.of(email)));
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  @Override public Iterable<Truck> findTrucksWithEmail() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.setFilter(Query.CompositeFilterOperator.and(new Query.FilterPredicate(TRUCK_EMAIL, Query.FilterOperator.NOT_EQUAL, null),
        new Query.FilterPredicate(TRUCK_ALLOW_SYSTEM_NOTIFICATIONS, Query.FilterOperator.EQUAL, true)));
    return executeQuery(dataStore, q, null);
  }

  @Override public void deleteAll() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(getKind());
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      keys.add(entity.getKey());
    }
    dataStore.delete(keys.build());
  }

  @Override public Set<Truck> findTrucksWithCalendars() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.setFilter(new Query.FilterPredicate(TRUCK_CALENDAR_URL, Query.FilterOperator.NOT_EQUAL, null));
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  protected Entity toEntity(Truck truck, Entity entity) {
    entity.setProperty(TRUCK_DISPLAY_EMAIL, truck.isDisplayEmailPublicly());
    entity.setProperty(TRUCK_NAME_FIELD, truck.getName());
    entity.setProperty(TRUCK_TWITTER_HANDLE, truck.getTwitterHandle());
    entity.setProperty(TRUCK_URL, truck.getUrl());
    entity.setProperty(TRUCK_PREVIEW_ICON, truck.getPreviewIcon());
    entity.setProperty(TRUCK_ICON_URL, truck.getIconUrl());
    entity.setProperty(TRUCK_BEACONNAISE_EMAILS, truck.getBeaconnaiseEmails());
    entity.setProperty(TRUCK_CALENDAR_URL,
        Strings.isNullOrEmpty(truck.getCalendarUrl()) ? null : truck.getCalendarUrl());
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
    entity.setProperty(TRUCK_PHONE, truck.getPhone());
    entity.setProperty(TRUCK_HIDDEN, truck.isHidden());
    entity.setProperty(TRUCK_INSTAGRAM, truck.getInstagramId());
    entity.setProperty(TRUCK_FULL_SIZE, truck.getFullsizeImage());
    entity.setProperty(SCAN_FACEBOOK, truck.getScanFacebook());
    entity.setProperty(LAST_SCANNED, truck.getLastScanned());
    entity.setProperty(TRUCK_ALLOW_SYSTEM_NOTIFICATIONS, truck.isAllowSystemNotifications());
    entity.setProperty(TRUCK_FACEBOOK_PAGE_ID, truck.getFacebookPageId());
    entity.setProperty(TRUCK_TWITTER_GEOLOCATION, truck.isTwitterGeolocationDataValid());
    Attributes.setDateProperty(TRUCK_MUTE_UNTIL, entity, truck.getMuteUntil());
    Truck.Stats stats = truck.getStats();
    if (stats == null) {
      stats = Truck.Stats.builder().build();
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
    entity.setProperty(TRUCK_STATS_LAST_UPDATED, stats.getLastUpdated().toDate());
    entity.setProperty(TRUCK_STATS_TOTAL_STOPS, stats.getTotalStops());
    entity.setProperty(TRUCK_STATS_STOPS_THIS_YEAR, stats.getStopsThisYear());
    return entity;
  }
}
