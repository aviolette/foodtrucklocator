package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;

import foodtruck.dao.AddressRuleDAO;
import foodtruck.model.AddressRuleScript;

/**
 * @author aviolette@gmail.com
 * @since 8/19/12
 */
public class AddressRuleScriptDAOAppEngine extends AppEngineDAO<Long, AddressRuleScript> implements
    AddressRuleDAO {

  @Inject
  public AddressRuleScriptDAOAppEngine(DatastoreServiceProvider provider) {
    super("address_rule_script", provider);
  }

  @Override protected Entity toEntity(AddressRuleScript addressRule, Entity entity) {
    entity.setProperty("script", addressRule.getScript());
    return entity;
  }

  @Override protected AddressRuleScript fromEntity(Entity entity) {
    return AddressRuleScript.builder()
        .script((String)entity.getProperty("script"))
        .build();
  }

  @Override public long save(AddressRuleScript obj) {
    DatastoreService service = provider.get();
    Query q = new Query(getKind());
    Entity entity = service.prepare(q).asSingleEntity();
    obj.validate();
    if (entity != null) {
    } else {
      entity = new Entity(getKind());
    }
    entity.setProperty("script", obj.getScript());
    service.put(entity);
    return entity.getKey().getId();
  }

  @Override public AddressRuleScript findSingleton() {
    DatastoreService service = provider.get();
    Query q = new Query(getKind());
    Entity entity = service.prepare(q).asSingleEntity();
    if (entity == null) {
      return AddressRuleScript.builder().build();
    }
    return fromEntity(entity);
  }
}
