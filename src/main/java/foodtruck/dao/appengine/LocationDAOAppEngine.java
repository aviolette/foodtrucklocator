package foodtruck.dao.appengine;

import java.util.logging.Level;
import java.util.logging.Logger;

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
public class LocationDAOAppEngine implements LocationDAO {
  private final DatastoreServiceProvider provider;
  private static final String LOCATION_KIND = "Location";
  private static final String NAME_FIELD = "name";
  private static final String LAT_FIELD = "lat";
  private static final String LNG_FIELD = "lng";
  private static final String TIMESTAMP_FIELD = "createdate";
  private static final Logger log = Logger.getLogger(LocationDAOAppEngine.class.getName());
  private final Clock clock;

  @Inject
  public LocationDAOAppEngine(DatastoreServiceProvider provider, Clock clock) {
    this.provider = provider;
    this.clock = clock;
  }

  @Override
  public Location lookup(String keyword) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(LOCATION_KIND);
    // TODO: fix so it searches in a case insensitive manner
    q.addFilter(NAME_FIELD, Query.FilterOperator.EQUAL, keyword);
    Entity entity = null;
    try {
      entity = dataStore.prepare(q).asSingleEntity();
    } catch (PreparedQuery.TooManyResultsException tmr) {
      log.log(Level.WARNING, "Got too many results exception for: {0}", keyword);
      try {
        deleteDuplicates(keyword, dataStore);
      } catch (Exception e) {
        log.log(Level.WARNING, "Error deleting duplicates", e);
      }
    }
    if (entity != null) {
      Double lat = (Double) entity.getProperty(LAT_FIELD);
      Double lng = (Double) entity.getProperty(LNG_FIELD);
      if (lat == null || lng == null) {
        return new Location(0, 0, keyword);
      }
      return new Location(lat, lng, keyword);
    }
    return null;
  }

  private void deleteDuplicates(String keyword, DatastoreService dataStore) {
    Query q = new Query(LOCATION_KIND);
    q.addFilter(NAME_FIELD, Query.FilterOperator.EQUAL, keyword);
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    boolean first = true;
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      if (!first) {
        keys.add(entity.getKey());
      }
      first = false;
    }
    dataStore.delete(keys.build());
  }

  @Override
  public void save(Location location) {
    DatastoreService dataStore = provider.get();
    final Entity entity = new Entity(LOCATION_KIND);
    entity.setProperty(NAME_FIELD, location.getName());
    entity.setProperty(LAT_FIELD, location.getLatitude());
    entity.setProperty(LNG_FIELD, location.getLongitude());
    entity.setProperty(TIMESTAMP_FIELD, clock.now().toDate());
    dataStore.put(entity);
  }

  @Override
  public void saveAttemptFailed(String location) {
    DatastoreService dataStore = provider.get();
    final Entity entity = new Entity(LOCATION_KIND);
    entity.setProperty(NAME_FIELD, location);
    dataStore.put(entity);
  }
}
