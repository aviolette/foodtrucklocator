package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.inject.Inject;

import foodtruck.dao.AddressRuleScriptDAO;
import foodtruck.model.AddressRuleScript;

/**
 * @author aviolette@gmail.com
 * @since 8/19/12
 */
class AddressRuleScriptDAOAppEngine extends SingletonDAOAppEngine<AddressRuleScript>
    implements AddressRuleScriptDAO {

  @Inject
  public AddressRuleScriptDAOAppEngine(DatastoreServiceProvider provider) {
    super(provider, "address_rule_script");
  }

  @Override protected AddressRuleScript fromEntity(Entity entity) {
    Text t = (Text) entity.getProperty("script");
    return AddressRuleScript.builder()
        .script(t.getValue())
        .build();
  }

  @Override protected Entity toEntity(Entity entity, AddressRuleScript obj) {
    entity.setProperty("script", new Text(obj.getScript()));
    return entity;
  }

  @Override protected AddressRuleScript buildObject() {
    return AddressRuleScript.builder().build();
  }
}
