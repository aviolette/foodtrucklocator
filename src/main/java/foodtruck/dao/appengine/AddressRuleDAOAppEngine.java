package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;

import foodtruck.dao.AddressRuleDAO;
import foodtruck.model.AddressRule;

/**
 * @author aviolette@gmail.com
 * @since 8/19/12
 */
public class AddressRuleDAOAppEngine extends AppEngineDAO<Long, AddressRule> implements
    AddressRuleDAO {

  @Inject
  public AddressRuleDAOAppEngine(DatastoreServiceProvider provider) {
    super("addressrule", provider);
  }

  @Override protected Entity toEntity(AddressRule addressRule, Entity entity) {
    entity.setProperty("pattern", addressRule.getPattern());
    return entity;
  }

  @Override protected AddressRule fromEntity(Entity entity) {
    return AddressRule.builder()
        .pattern((String)entity.getProperty("pattern"))
        .build();
  }
}
