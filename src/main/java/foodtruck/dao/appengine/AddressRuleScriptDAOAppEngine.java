package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;

import foodtruck.dao.AddressRuleScriptDAO;
import foodtruck.model.AddressRuleScript;

/**
 * @author aviolette@gmail.com
 * @since 8/19/12
 */
public class AddressRuleScriptDAOAppEngine extends AppEngineSingletonDAO<AddressRuleScript>
    implements AddressRuleScriptDAO {

  @Inject
  public AddressRuleScriptDAOAppEngine(DatastoreServiceProvider provider) {
    super(provider, "address_rule_script");
  }

  @Override protected AddressRuleScript fromEntity(Entity entity) {
    return AddressRuleScript.builder()
        .script((String) entity.getProperty("script"))
        .build();
  }

  @Override protected Entity toEntity(Entity entity, AddressRuleScript obj) {
    entity.setProperty("script", obj.getScript());
    return entity;
  }

  @Override protected AddressRuleScript buildObject() {
    return AddressRuleScript.builder().build();
  }
}
