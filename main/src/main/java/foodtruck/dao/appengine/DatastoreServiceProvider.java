package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.inject.Provider;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class DatastoreServiceProvider implements Provider<DatastoreService> {
  @Override
  public DatastoreService get() {
    return DatastoreServiceFactory.getDatastoreService();
  }
}
