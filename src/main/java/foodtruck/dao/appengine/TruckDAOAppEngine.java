package foodtruck.dao.appengine;

import java.util.Collection;
import java.util.Set;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.dao.appengine.Attributes.getLongProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;

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
      Location loc = null;
      if (entity.hasProperty(TRUCK_STATS_LAST_SEEN_WHERE)) {
        loc = Location.builder()
            .lat(getDoubleProperty(entity, TRUCK_STATS_LAST_SEEN_WHERE_LAT, 0))
            .lng(getDoubleProperty(entity, TRUCK_STATS_LAST_SEEN_WHERE_LNG, 0))
            .name(getStringProperty(entity, TRUCK_STATS_LAST_SEEN_WHERE))
            .build();
      }
      stats = Truck.Stats.builder()
          .lastSeen(getDateTime(entity, TRUCK_STATS_LAST_SEEN_WHEN, zone))
          .lastUpdate(getDateTime(entity, TRUCK_STATS_LAST_UPDATED, zone))
          .stopsThisYear(getLongProperty(entity, TRUCK_STATS_STOPS_THIS_YEAR, 0))
          .totalStops(getLongProperty(entity, TRUCK_STATS_TOTAL_STOPS, 0))
          .whereLastSeen(loc)
          .build();
    }

    return builder.id(entity.getKey().getName())
        .stats(stats)
        .inactive((Boolean) entity.getProperty(INACTIVE_FIELD))
        .twitterHandle((String) entity.getProperty(TRUCK_TWITTER_HANDLE))
        .defaultCity((String) entity.getProperty(TRUCK_DEFAULT_CITY_FIELD))
        .description(t == null ? null : t.getValue())
        .facebook((String) entity.getProperty(TRUCK_FACEBOOK_FIELD))
        .foursquareUrl((String) entity.getProperty(TRUCK_FOURSQUARE_URL_FIELD))
        .iconUrl((String) entity.getProperty(TRUCK_ICON_URL))
        .muteUntil(Attributes.getDateTime(entity, TRUCK_MUTE_UNTIL, zone))
        .name((String) entity.getProperty(TRUCK_NAME_FIELD))
        .yelpSlug(getStringProperty(entity, TRUCK_YELP_SLUG))
        .facebookPageId(getStringProperty(entity, TRUCK_FACEBOOK_PAGE_ID))
        .matchOnlyIf((String) entity.getProperty(MATCH_REGEX_FIELD))
        .donotMatchIf((String) entity.getProperty(DONT_MATCH_REGEX_FIELD))
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


  @Override public Collection<Truck> findByTwitterId(String screenName) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.addFilter(TRUCK_TWITTER_HANDLE, Query.FilterOperator.EQUAL, screenName);
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  @Override public Collection<Truck> findAllTwitterTrucks() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.addFilter(TRUCK_TWITTALYZER_FIELD, Query.FilterOperator.EQUAL, true);
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  @Override public Collection<Truck> findActiveTrucks() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(getKind());
    q.addFilter(INACTIVE_FIELD, Query.FilterOperator.NOT_EQUAL, true);
    ImmutableSet.Builder<Truck> objs = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Truck obj = fromEntity(entity);
      objs.add(obj);
    }
    return objs.build();
  }

  @Override public Set<Truck> findTrucksWithCalendars() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.addFilter(TRUCK_CALENDAR_URL, Query.FilterOperator.NOT_EQUAL, null);
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  protected Entity toEntity(Truck truck, Entity entity) {
    entity.setProperty(TRUCK_NAME_FIELD, truck.getName());
    entity.setProperty(TRUCK_TWITTER_HANDLE, truck.getTwitterHandle());
    entity.setProperty(TRUCK_URL, truck.getUrl());
    entity.setProperty(TRUCK_ICON_URL, truck.getIconUrl());
    entity.setProperty(TRUCK_CALENDAR_URL,
        Strings.isNullOrEmpty(truck.getCalendarUrl()) ? null : truck.getCalendarUrl());
    entity.setProperty(TRUCK_DESCRIPTION_FIELD, new Text(truck.getDescription() == null ? "" : truck.getDefaultCity()));
    entity.setProperty(TRUCK_FOURSQUARE_URL_FIELD, truck.getFoursquareUrl());
    entity.setProperty(TRUCK_TWITTALYZER_FIELD, truck.isUsingTwittalyzer());
    entity.setProperty(TRUCK_DEFAULT_CITY_FIELD, truck.getDefaultCity());
    entity.setProperty(TRUCK_FACEBOOK_FIELD, truck.getFacebook());
    entity.setProperty(MATCH_REGEX_FIELD, truck.getMatchOnlyIfString());
    entity.setProperty(DONT_MATCH_REGEX_FIELD, truck.getDonotMatchIfString());
    entity.setProperty(INACTIVE_FIELD, truck.isInactive());
    entity.setProperty(CATEGORIES_FIELD, truck.getCategories());
    entity.setProperty(TRUCK_EMAIL, truck.getEmail());
    entity.setProperty(TRUCK_YELP_SLUG, truck.getYelpSlug());
    entity.setProperty(TRUCK_PHONE, truck.getPhone());
    entity.setProperty(TRUCK_FACEBOOK_PAGE_ID, truck.getFacebookPageId());
    entity.setProperty(TRUCK_TWITTER_GEOLOCATION, truck.isTwitterGeolocationDataValid());
    Attributes.setDateProperty(TRUCK_MUTE_UNTIL, entity, truck.getMuteUntil());
    Truck.Stats stats = truck.getStats();
    if (stats == null) {
      stats = Truck.Stats.builder().build();
    }
    final DateTime lastSeen = stats.getLastSeen();
    if (lastSeen == null) {
      entity.setProperty(TRUCK_STATS_LAST_SEEN_WHEN, null);
    } else {
      entity.setProperty(TRUCK_STATS_LAST_SEEN_WHEN, lastSeen.toDate());
    }
    Location loc = stats.getWhereLastSeen();
    if (loc != null) {
      entity.setProperty(TRUCK_STATS_LAST_SEEN_WHERE, loc.getName());
      entity.setProperty(TRUCK_STATS_LAST_SEEN_WHERE_LAT, loc.getLatitude());
      entity.setProperty(TRUCK_STATS_LAST_SEEN_WHERE_LNG, loc.getLongitude());
    }
    entity.setProperty(TRUCK_STATS_LAST_UPDATED, stats.getLastUpdated().toDate());
    entity.setProperty(TRUCK_STATS_TOTAL_STOPS, stats.getTotalStops());
    entity.setProperty(TRUCK_STATS_STOPS_THIS_YEAR, stats.getStopsThisYear());
    return entity;
  }
}
