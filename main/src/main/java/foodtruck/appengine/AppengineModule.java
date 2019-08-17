package foodtruck.appengine;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import foodtruck.appengine.cache.MemcacheCacher;
import foodtruck.appengine.dao.appengine.AppEngineDAOModule;
import foodtruck.appengine.dao.memcached.MemcachedModule;
import foodtruck.appengine.kms.GoogleCryptoModule;
import foodtruck.appengine.storage.GcsStorageService;
import foodtruck.caching.Cacher;
import foodtruck.model.Environment;
import foodtruck.storage.StorageService;
import foodtruck.user.LoggedInUser;

/**
 * @author aviolette
 * @since 11/23/16
 */
public class AppengineModule extends AbstractModule {
  private static final Logger log = Logger.getLogger(AppengineModule.class.getName());

  @Override
  protected void configure() {
    install(new AppEngineDAOModule());
    install(new MemcachedModule());
    install(new GoogleCryptoModule());
    bind(Cacher.class).to(MemcacheCacher.class);
    bind(StorageService.class).to(GcsStorageService.class);
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

  @Provides
  @Singleton
  public HttpTransport providesHttpTransport() {
    return new UrlFetchTransport();
  }

  @Provides
  @Named("projectId")
  @Singleton
  public String providesProjectId() {
    return AppIdentityServiceFactory.getAppIdentityService()
        .getServiceAccountName()
        .split("@")[0];
  }

  @Provides
  @Singleton
  public HttpRequestInitializer getRequestInitializer(JsonFactory jsonFactory) {
    SystemProperty.Environment.Value env = SystemProperty.environment.value();
    if (env != SystemProperty.Environment.Value.Production) {
      try {

        return new GoogleCredential.Builder().setTransport(new NetHttpTransport())
            .setJsonFactory(jsonFactory)
            .setServiceAccountId("891347525506-0ktp2j5ll7ifbt766ii64p207q67vjm7@developer.gserviceaccount.com")
            .setServiceAccountScopes(ImmutableList.of("https://www.googleapis.com/auth/calendar"))
            .setServiceAccountPrivateKeyFromP12File(new File(
                System.getProperty("user.home") + File.separator + ".store" + File.separator + "/google_auth.p12"))
            .build();
      } catch (Exception e) {
        log.log(Level.SEVERE, e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }
    log.info("Using production credentials for Google APIs");
    return new AppIdentityCredential(ImmutableList.of("https://www.googleapis.com/auth/calendar"));
  }

  @Provides
  @Singleton
  public JsonFactory providesJsonFactory() {
    return new JacksonFactory();
  }

  @Provides
  public Environment providesEnvironment() {
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development ? Environment.Development : Environment.Production;
  }

  @Singleton
  @Provides
  public GcsService providesGoogleCloudStorage() {
    return GcsServiceFactory.createGcsService(new RetryParams.Builder().initialRetryDelayMillis(10)
        .retryMaxAttempts(10)
        .totalRetryPeriodMillis(15000)
        .build());
  }

  @Provides
  public Optional<LoggedInUser> providesUser(UserService userService) {
    if (!userService.isUserLoggedIn()) {
      return Optional.absent();
    }
    return Optional.of(new LoggedInUser(userService.getCurrentUser()
        .getEmail(), userService.isUserAdmin()));
  }
}
