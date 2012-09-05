package foodtruck.dao.appengine;

import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import foodtruck.dao.AddressRuleTestDAO;
import foodtruck.model.AddressRuleTest;

/**
 * @author aviolette@gmail.com
 * @since 9/4/12
 */
public class AddressRuleTestDAOAppEngine extends AppEngineDAO<Long, AddressRuleTest> implements
    AddressRuleTestDAO {

  private static final String PROP_NAME = "name";
  private static final String PROP_EXPECTED = "expected";
  private static final String PROP_INPUT = "input";
  private static final String PROP_TRUCK_ID = "truck_id";

  @Inject
  public AddressRuleTestDAOAppEngine(DatastoreServiceProvider provider) {
    super("address_rule_test", provider);
  }

  @Override public List<AddressRuleTest> findAllByName() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(getKind());
    q.addSort(PROP_NAME, Query.SortDirection.ASCENDING);
    ImmutableList.Builder<AddressRuleTest> objs = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      AddressRuleTest obj = fromEntity(entity);
      objs.add(obj);
    }
    return objs.build();
  }

  @Override protected Entity toEntity(AddressRuleTest obj, Entity entity) {
    entity.setProperty(PROP_NAME, obj.getName());
    entity.setProperty(PROP_EXPECTED, obj.getExpected());
    entity.setProperty(PROP_INPUT, obj.getInput());
    entity.setProperty(PROP_TRUCK_ID, obj.getTruck());
    return entity;
  }

  @Override protected AddressRuleTest fromEntity(Entity entity) {
    return AddressRuleTest.builder()
        .name((String)entity.getProperty(PROP_NAME))
        .expected((String)entity.getProperty(PROP_EXPECTED))
        .input((String)entity.getProperty(PROP_INPUT))
        .truck((String)entity.getProperty(PROP_TRUCK_ID))
        .key(entity.getKey().getId())
        .build();
  }
}
