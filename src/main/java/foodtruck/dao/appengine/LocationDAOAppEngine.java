package foodtruck.dao.appengine;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;

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
  private static final Logger log = Logger.getLogger(LocationDAOAppEngine.class.getName());

  @Inject
  public LocationDAOAppEngine(DatastoreServiceProvider provider) {
    this.provider = provider;
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
      log.log(Level.WARNING, "Got too many results exception for: '{0}'", keyword);
    }

    if (entity != null) {
      return new Location((Double) entity.getProperty(LAT_FIELD),
          (Double) entity.getProperty(LNG_FIELD), keyword);
    }
    return null;
  }

  @Override
  public void save(Location location) {
    DatastoreService dataStore = provider.get();
    final Entity entity = new Entity(LOCATION_KIND);
    entity.setProperty(NAME_FIELD, location.getName());
    entity.setProperty(LAT_FIELD, location.getLatitude());
    entity.setProperty(LNG_FIELD, location.getLongitude());
    dataStore.put(entity);
  }
}
