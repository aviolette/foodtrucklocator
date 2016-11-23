package foodtruck.appengine;

import java.util.logging.Level;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;

/**
 * @author aviolette
 * @since 11/23/16
 */
public class AppengineModule extends PrivateModule {
  @Override
  protected void configure() {
  }

  @Provides
  @Exposed
  public MemcacheService provideMemcacheService() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    return syncCache;
  }

  @Provides
  @Exposed
  public UserService provideUserService() {
    return UserServiceFactory.getUserService();
  }

  @Provides
  @Exposed
  public Queue provideQueue() {
    return QueueFactory.getDefaultQueue();
  }

  @Provides
  @Exposed
  public DatastoreService provideDatastoreService() {
    return DatastoreServiceFactory.getDatastoreService();
  }
}
