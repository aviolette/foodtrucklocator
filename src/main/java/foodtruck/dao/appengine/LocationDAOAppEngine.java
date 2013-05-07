package foodtruck.dao.appengine;

import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class LocationDAOAppEngine extends AppEngineDAO<Long, Location> implements LocationDAO {
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

  private static final Logger log = Logger.getLogger(LocationDAOAppEngine.class.getName());
  private final Clock clock;

  @Inject
  public LocationDAOAppEngine(DatastoreServiceProvider provider, Clock clock) {
    super(LOCATION_KIND, provider);
    this.clock = clock;
  }

  @Override
  public @Nullable Location findByAddress(String keyword) {
    DatastoreService dataStore = provider.get();
    Query q = locationQuery(keyword);
    Entity entity = null;
    try {
      entity = dataStore.prepare(q).asSingleEntity();
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

  @Override public Collection<Location> findAllNonMigrated() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(LOCATION_KIND);
    return executeQuery(dataStore,  q);
  }

  @Override public Set<Location> findPopularLocations() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(LOCATION_KIND);
    Query.Filter popularFilter = new Query.FilterPredicate(POPULAR_FIELD, Query.FilterOperator.EQUAL, true);
    q.setFilter(popularFilter);
    return executeQuery(dataStore, q);
  }

  private Query locationQuery(String keyword) {
    Query q = new Query(LOCATION_KIND);
    Query.Filter nameFilter = new Query.FilterPredicate(NAME_FIELD, Query.FilterOperator.EQUAL, keyword);
    q.setFilter(nameFilter);
    return q;
  }

  private Entity deleteDuplicates(String keyword, DatastoreService dataStore) {
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    Entity firstEntity = null;
    for (Entity entity : dataStore.prepare(locationQuery(keyword)).asIterable()) {
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

  @Override protected Entity toEntity(Location location, Entity entity) {
    entity.setProperty(NAME_FIELD, location.getName());
    entity.setProperty(LAT_FIELD, location.getLatitude());
    entity.setProperty(LNG_FIELD, location.getLongitude());
    entity.setProperty(TIMESTAMP_FIELD, clock.now().toDate());
    entity.setProperty(VALID_FIELD, location.isValid());
    entity.setProperty(DESCRIPTION_FIELD, location.getDescription());
    entity.setProperty(URL_FIELD, location.getUrl());
    entity.setProperty(RADIAL_FIELD, location.getRadius());
    entity.setProperty(LOCATION_LOOKUP_FIELD, location.getName().toLowerCase());
    entity.setProperty(POPULAR_FIELD, location.isPopular());
    return entity;
  }

  @Override protected Location fromEntity(Entity entity) {
    Double lat = (Double) entity.getProperty(LAT_FIELD);
    Double lng = (Double) entity.getProperty(LNG_FIELD);
    Boolean valid = (Boolean) entity.getProperty(VALID_FIELD);
    Object key = entity.getKey().getId();
    Location.Builder builder =
        Location.builder().name((String) entity.getProperty(NAME_FIELD)).key(key);
    builder.description((String) entity.getProperty(DESCRIPTION_FIELD));
    builder.url((String) entity.getProperty(URL_FIELD));
    builder.popular(getBooleanProperty(entity, POPULAR_FIELD, false));
    builder.radius(Attributes.getDoubleProperty(entity, RADIAL_FIELD, 0.0));
    boolean isValid = valid == null || valid;
    if (lat == null || lng == null) {
      builder.valid(false);
    } else {
      builder.lat(lat).lng(lng).valid(isValid);
    }
    return builder.build();
  }
}
