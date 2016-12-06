package foodtruck.dao.datastore;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import foodtruck.dao.AddressRuleScriptDAO;
import foodtruck.dao.TruckDAO;

/**
 * @author aviolette
 * @since 10/13/16
 */
public class AppengineDAOModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AddressRuleScriptDAO.class).to(AddressRuleScriptDAOAppEngine.class);
    bind(TruckDAO.class).to(TruckDAOAppEngine.class);
  }

  @Provides
  public Datastore provideDatastore() {
    return DatastoreOptions.defaultInstance().service();
  }
}
