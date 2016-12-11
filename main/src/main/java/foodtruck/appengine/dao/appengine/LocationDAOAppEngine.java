package foodtruck.appengine.dao.appengine;

import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.time.Clock;

import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.or;
import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.IN;
import static com.google.appengine.api.datastore.Query.FilterOperator.NOT_EQUAL;
import static foodtruck.appengine.dao.appengine.Attributes.getIntProperty;
import static foodtruck.appengine.dao.appengine.Attributes.getSetProperty;
import static foodtruck.appengine.dao.appengine.Attributes.getStringProperty;
import static foodtruck.appengine.dao.appengine.Attributes.getUrlProperty;
import static foodtruck.appengine.dao.appengine.Attributes.setUrlProperty;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
class LocationDAOAppEngine extends AppEngineDAO<Long, Location> implements LocationDAO {
  private static final String LOCATION_KIND = "Location";
  private static final String NAME_FIELD = "name";
  private static final String LAT_FIELD = "lat";
  private static final String LNG_FIELD = "lng";
  private static final String TIMESTAMP_FIELD = "createdate";
  private static final String VALID_FIELD = "valid";
  private static final String DESCRIPTION_FIELD = "description";
  private static final String URL_FIELD = "url";
  private static final String RADIAL_FIELD = "radial_boundary";
  private static final String LOCATION_LOOKUP_FIELD = "location_lookup";
  private static final String POPULAR_FIELD = "popular";
  private static final String AUTOCOMPLETE = "autocomplete";
  private static final String ALIAS = "alias";
  private static final String TWITTERHANDLE = "twitter_handle";
  private static final String DESIGNATED_STOP = "designated_stop";
  private static final String HAS_BOOZE = "has_booze";
  private static final String OWNED_BY = "owned_by";
  private static final String RADIATE_TO = "radiate_to";
  private static final String PHONE = "phone";
  private static final String EMAIL = "email";
  private static final String FACEBOOK_URI = "facebook_uri";
  private static final String CLOSED = "closed";
  private static final String IMAGE_URL = "image_url";
  private static final String EVENT_URL = "event_url";
  private static final String MANAGER_EMAILS = "manager_emails";
  private static final String REVERSE_LOOKUP_KEY = "reverse_lookup_key";
  private static final String ALEXA_PROVIDED = "alexa_provided";
  private static final String CREATED_BY = "created_by";

  private static final Logger log = Logger.getLogger(LocationDAOAppEngine.class.getName());
  private final Clock clock;

  @Inject
  public LocationDAOAppEngine(Provider<DatastoreService> provider, Clock clock) {
    super(LOCATION_KIND, provider);
    this.clock = clock;
  }

  @Nullable
  @Override
  public Location findByAddress(String keyword) {
    DatastoreService dataStore = provider.get();
    Entity entity = null;
    try {
      entity = dataStore.prepare(locationQuery(keyword))
          .asSingleEntity();
    } catch (PreparedQuery.TooManyResultsException tmr) {
      log.log(Level.WARNING, "Got too many results exception for: {0}", keyword);
      try {
        entity = deleteDuplicates(keyword, dataStore);
      } catch (Exception e) {
        log.log(Level.WARNING, "Error deleting duplicates", e);
      }
    }
    if (entity != null) {
      return fromEntity(entity);
    }
    return null;
  }

  @Nullable
  @Override
  public Location findByLatLng(Location location) {
    String lookupKey = reverseLookupKey(location);
    log.log(Level.INFO, "Reverse lookup key: " + lookupKey);
    return aq().filter(predicate(REVERSE_LOOKUP_KEY, EQUAL, lookupKey))
        .findFirst();
  }

  @Override
  public List<Location> findPopularLocations() {
    return aq().filter(predicate(POPULAR_FIELD, EQUAL, true))
        .sort(NAME_FIELD)
        .execute();
  }

  @Override
  public List<Location> findAutocompleteLocations() {
    return aq().filter(or(predicate(POPULAR_FIELD, EQUAL, true), predicate(AUTOCOMPLETE, EQUAL, true)))
        .sort(NAME_FIELD)
        .execute();
  }

  @Override
  public List<Location> findLocationsOwnedByFoodTrucks() {
    return aq().filter(predicate(OWNED_BY, NOT_EQUAL, null))
        .execute();
  }

  @Override
  public List<Location> findAliasesFor(String locationName) {
    return aq().filter(predicate(ALIAS, EQUAL, locationName))
        .execute();
  }

  @Override
  public Collection<Location> findDesignatedStops() {
    return aq().filter(predicate(DESIGNATED_STOP, EQUAL, true))
        .execute();
  }

  @Override
  public Iterable<Location> findBoozyLocations() {
    return aq().filter(predicate(HAS_BOOZE, EQUAL, true))
        .execute();
  }

  @Override
  public Collection<Location> findByTwitterId(String twitterId) {
    return aq().filter(predicate(TWITTERHANDLE, EQUAL, twitterId.toLowerCase()))
        .execute();
  }

  @Override
  public Collection<Location> findByManagerEmail(String email) {
    return aq().filter(predicate(MANAGER_EMAILS, IN, ImmutableSet.of(email)))
        .execute();
  }

  @Override
  public List<Location> findAlexaStops() {
    return aq().filter(predicate(ALEXA_PROVIDED, EQUAL, true))
        .sort(NAME_FIELD)
        .execute();
  }

  @Nullable
  @Override
  public Location findByAlias(String location) {
    // max of three marches up the alias-tree
    for (int i = 0; i < 3; i++) {
      Location loc = findByAddress(location);
      if (loc == null || Strings.isNullOrEmpty(loc.getAlias())) {
        return loc;
      }
      location = loc.getAlias();
    }
    return null;
  }

  private Query locationQuery(String keyword) {
    return query().setFilter(predicate(NAME_FIELD, EQUAL, keyword));
  }

  private Entity deleteDuplicates(String keyword, DatastoreService dataStore) {
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    Entity firstEntity = null;
    for (Entity entity : dataStore.prepare(locationQuery(keyword))
        .asIterable()) {
      if (firstEntity != null) {
        keys.add(entity.getKey());
      } else {
        firstEntity = entity;
      }
    }
    dataStore.delete(keys.build());
    return firstEntity;
  }

  @Override
  public Location saveAndFetch(Location location) {
    long id = save(location);
    return location.withKey(id);
  }

  @Override
  protected Entity toEntity(Location location, Entity entity) {
    entity.setProperty(NAME_FIELD, location.getName());
    entity.setProperty(LAT_FIELD, location.getLatitude());
    entity.setProperty(LNG_FIELD, location.getLongitude());
    entity.setProperty(TIMESTAMP_FIELD, clock.now()
        .toDate());
    entity.setProperty(VALID_FIELD, location.isValid());
    entity.setProperty(DESCRIPTION_FIELD, location.getDescription());
    entity.setProperty(URL_FIELD, location.getUrl());
    entity.setProperty(RADIAL_FIELD, location.getRadius());
    entity.setProperty(LOCATION_LOOKUP_FIELD, location.getName()
        .toLowerCase());
    entity.setProperty(POPULAR_FIELD, location.isPopular());
    entity.setProperty(AUTOCOMPLETE, location.isAutocomplete());
    entity.setProperty(ALIAS, location.getAlias());
    entity.setProperty(TWITTERHANDLE, location.getTwitterHandle());
    entity.setProperty(DESIGNATED_STOP, location.isDesignatedStop());
    entity.setProperty(OWNED_BY, location.getOwnedBy());
    entity.setProperty(MANAGER_EMAILS, location.getManagerEmails());
    entity.setProperty(HAS_BOOZE, location.isHasBooze());
    entity.setProperty(RADIATE_TO, location.getRadiateTo());
    entity.setProperty(PHONE, location.getPhoneNumber());
    entity.setProperty(EMAIL, location.getEmail());
    entity.setProperty(FACEBOOK_URI, location.getFacebookUri());
    entity.setProperty(CLOSED, location.isClosed());
    entity.setProperty(REVERSE_LOOKUP_KEY, reverseLookupKey(location));
    setUrlProperty(entity, IMAGE_URL, location.getImageUrl());
    entity.setProperty(EVENT_URL, location.getEventCalendarUrl());
    entity.setProperty(ALEXA_PROVIDED, location.isAlexaProvided());
    entity.setProperty(CREATED_BY, location.getCreatedBy());
    return entity;
  }

  private String reverseLookupKey(Location location) {
    StringBuilder builder = new StringBuilder();
    Formatter formatter = new Formatter(builder, Locale.US);
    formatter.format("%10.4f %10.4f", location.getLatitude(), location.getLongitude());
    String s = builder.toString();
    return s.replaceAll(" ", "");
  }

  @Override
  protected Location fromEntity(Entity entity) {
    Double lat = (Double) entity.getProperty(LAT_FIELD);
    Double lng = (Double) entity.getProperty(LNG_FIELD);
    Boolean valid = (Boolean) entity.getProperty(VALID_FIELD);
    Object key = entity.getKey()
        .getId();
    Location.Builder builder = Location.builder()
        .name((String) entity.getProperty(NAME_FIELD))
        .key(key);
    builder.description((String) entity.getProperty(DESCRIPTION_FIELD));
    builder.url((String) entity.getProperty(URL_FIELD));
    builder.popular(getBooleanProperty(entity, POPULAR_FIELD, false));
    builder.autocomplete(getBooleanProperty(entity, AUTOCOMPLETE, false));
    builder.alias(getStringProperty(entity, ALIAS));
    builder.eventCalendarUrl(getStringProperty(entity, EVENT_URL));
    builder.radius(Attributes.getDoubleProperty(entity, RADIAL_FIELD, 0.0));
    builder.twitterHandle(Attributes.getStringProperty(entity, TWITTERHANDLE));
    builder.ownedBy(getStringProperty(entity, OWNED_BY));
    builder.designatedStop(getBooleanProperty(entity, DESIGNATED_STOP, false));
    builder.hasBooze(getBooleanProperty(entity, HAS_BOOZE, false));
    builder.managerEmails(getSetProperty(entity, MANAGER_EMAILS));
    builder.radiateTo(getIntProperty(entity, RADIATE_TO, 0));
    builder.phoneNumber(getStringProperty(entity, PHONE));
    builder.email(getStringProperty(entity, EMAIL));
    builder.facebookUri(getStringProperty(entity, FACEBOOK_URI));
    builder.closed(getBooleanProperty(entity, CLOSED, false));
    builder.imageUrl(getUrlProperty(entity, IMAGE_URL));
    builder.createdBy(getStringProperty(entity, CREATED_BY));
    builder.alexaProvided(getBooleanProperty(entity, ALEXA_PROVIDED, false));
    boolean isValid = valid == null || valid;
    if (lat == null || lng == null) {
      builder.valid(false);
    } else {
      builder.lat(lat)
          .lng(lng)
          .valid(isValid);
    }
    return builder.build();
  }
}
