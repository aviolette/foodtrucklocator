package foodtruck.appengine.dao.appengine;

import java.util.Optional;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.ReverseLookupDAO;
import foodtruck.model.PartialLocation;

import static foodtruck.appengine.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.appengine.dao.appengine.Attributes.getStringProperty;

/**
 * @author aviolette
 * @since 10/7/18
 */
public class ReverseLookupDAOAppEngine extends AppEngineDAO<String, PartialLocation> implements ReverseLookupDAO {

  private static final Logger log = Logger.getLogger(ReverseLookupDAOAppEngine.class.getName());

  @Inject
  public ReverseLookupDAOAppEngine(Provider<DatastoreService> provider) {
    super("reverse_lookup", provider);
  }

  @Override
  protected Entity toEntity(PartialLocation obj, Entity entity) {
    entity.setProperty("name", obj.getName());
    entity.setProperty("lat", obj.getLat());
    entity.setProperty("lng", obj.getLng());
    return entity;
  }

  @Override
  protected PartialLocation fromEntity(Entity entity) {
    return new PartialLocation(getStringProperty(entity, "name"), getDoubleProperty(entity, "lat", 0),
        getDoubleProperty(entity, "lng", 0));
  }

  @Override
  public Optional<PartialLocation> findByLatLng(double lat, double lng) {
    String key = PartialLocation.reverseLookupKey(lat, lng);
    log.info("Looking up location by key: " + key);
    return findByIdOpt(key);
  }
}
