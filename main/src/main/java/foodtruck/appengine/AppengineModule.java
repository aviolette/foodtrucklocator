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
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import foodtruck.appengine.dao.appengine.AppEngineDAOModule;
import foodtruck.appengine.dao.memcached.MemcachedModule;

/**
 * @author aviolette
 * @since 11/23/16
 */
public class AppengineModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new AppEngineDAOModule());
    install(new MemcachedModule());
  }

  @Provides
  public MemcacheService provideMemcacheService() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    return syncCache;
  }

  @Provides
  public UserService provideUserService() {
    return UserServiceFactory.getUserService();
  }

  @Provides
  public Queue provideQueue() {
    return QueueFactory.getDefaultQueue();
  }

  @Provides
  public DatastoreService provideDatastoreService() {
    return DatastoreServiceFactory.getDatastoreService();
  }
}
