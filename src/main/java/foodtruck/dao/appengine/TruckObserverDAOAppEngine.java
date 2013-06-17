package foodtruck.dao.appengine;

import java.util.Collection;

import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.dao.TruckObserverDAO;
import foodtruck.model.Location;
import foodtruck.model.TruckObserver;
import static foodtruck.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;

/**
 * @author aviolette
 * @since 6/6/13
 */
public class TruckObserverDAOAppEngine extends AppEngineDAO<String, TruckObserver> implements TruckObserverDAO {
  private static final String TRUCK_OBSERVER_KIND = "truck_observer";
  private static final String NAME_FIELD = "location_name";
  private static final String LAT_FIELD = "lat_field";
  private static final String LNG_FIELD = "lng_field";
  private static final String KEYWORDS_FIELD = "keywords";

  @Inject
  public TruckObserverDAOAppEngine(DatastoreServiceProvider provider) {
    super(TRUCK_OBSERVER_KIND, provider);
  }

  @Override protected Entity toEntity(TruckObserver obj, Entity entity) {
    entity.setProperty(NAME_FIELD, obj.getLocation().getName());
    entity.setProperty(LAT_FIELD, obj.getLocation().getLatitude());
    entity.setProperty(LNG_FIELD, obj.getLocation().getLongitude());
    entity.setProperty(KEYWORDS_FIELD, obj.getKeywords());
    return entity;
  }

  @Override protected TruckObserver fromEntity(Entity entity) {
    Location location = Location.builder().name(getStringProperty(entity, NAME_FIELD))
        .lat(getDoubleProperty(entity, LAT_FIELD, 0.0d))
        .lng(getDoubleProperty(entity, LNG_FIELD, 0.0d))
        .build();
    return new TruckObserver(entity.getKey().getName(), location,
        ImmutableList.copyOf((Collection) entity.getProperty(KEYWORDS_FIELD)));
  }
}
