package foodtruck.appengine.dao.appengine;

import java.util.List;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.LinxupAccountDAO;
import foodtruck.model.LinxupAccount;

/**
 * @author aviolette
 * @since 10/18/16
 */
public class LinxupDAOAppengine extends AppEngineDAO<Long, LinxupAccount> implements LinxupAccountDAO {

  @Inject
  public LinxupDAOAppengine(Provider<DatastoreService> provider) {
    super("linxup_account", provider);
  }

  @Override
  protected Entity toEntity(LinxupAccount obj, Entity entity) {
    entity.setProperty("username", obj.getUsername());
    entity.setProperty("password", obj.getPassword());
    entity.setProperty("truck_id", obj.getTruckId());
    return entity;
  }

  @Override
  protected LinxupAccount fromEntity(Entity entity) {
    return LinxupAccount.builder()
        .key(entity.getKey()
            .getId())
        .username(Attributes.getStringProperty(entity, "username"))
        .password(Attributes.getStringProperty(entity, "password"))
        .truckId(Attributes.getStringProperty(entity, "truck_id"))
        .build();
  }

  @Nullable
  @Override
  public LinxupAccount findByTruck(String truckId) {
    return aq().filter(predicate("truck_id", Query.FilterOperator.EQUAL, truckId))
        .findFirst();
  }

  @Override
  public List<LinxupAccount> findActive() {
    return findAll();
  }
}
