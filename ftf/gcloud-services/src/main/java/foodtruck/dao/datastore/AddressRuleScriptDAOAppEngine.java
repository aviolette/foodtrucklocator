package foodtruck.dao.datastore;

import com.google.cloud.datastore.BaseEntity;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
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
  public AddressRuleScriptDAOAppEngine(Datastore datastore) {
    super(datastore, "address_rule_script");
  }

  @Override protected AddressRuleScript fromEntity(Entity entity) {
    return AddressRuleScript.builder()
        .key(entity.key().id())
        .script(entity.getString("script"))
        .build();
  }

  @Override
  protected void toEntity(AddressRuleScript obj, BaseEntity.Builder builder) {
    builder.set("script", obj.getScript());
  }

  @Override protected AddressRuleScript buildObject() {
    return AddressRuleScript.builder().build();
  }
}
