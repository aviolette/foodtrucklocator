package foodtruck.appengine.dao.appengine;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.MenuDAO;
import foodtruck.model.Menu;
import foodtruck.time.Clock;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;

/**
 * @author aviolette
 * @since 8/19/16
 */
class MenuDAOAppEngine extends AppEngineDAO<Long, Menu> implements MenuDAO {
  private static final String MENU = "menu";
  private static final String TRUCK_ID = "truck_id";
  private static final String MODIFIED_DATE = "modified";
  private final Clock clock;

  @Inject
  public MenuDAOAppEngine(Provider<DatastoreService> provider, Clock clock) {
    super("menu", provider);
    this.clock = clock;
  }

  @Nullable
  @Override
  public Menu findByTruck(String truckId) {
    return aq().filter(predicate(TRUCK_ID, EQUAL, truckId))
        .findOne();
  }

  @Override
  protected Entity toEntity(Menu obj, Entity entity) {
    Attributes.setDateProperty(MODIFIED_DATE, entity, clock.now());
    entity.setProperty(TRUCK_ID, obj.getTruckId());
    entity.setProperty(MENU, new Text(obj.getPayload()));
    return entity;
  }

  @Override
  protected Menu fromEntity(Entity entity) {
    return Menu.builder()
        .key(entity.getKey().getId())
        .truckId((String) entity.getProperty(TRUCK_ID))
        .payload(((Text) entity.getProperty(MENU)).getValue())
        .build();
  }
}
