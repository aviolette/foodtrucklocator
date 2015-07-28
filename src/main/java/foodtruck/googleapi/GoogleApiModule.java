package foodtruck.googleapi;

import java.io.File;
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
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 11/20/14
 */
public class GoogleApiModule extends PrivateModule {
  private static final Logger log = Logger.getLogger(GoogleApiModule.class.getName());

  @Override
  protected void configure() {
  }

  @Provides @Singleton
  public JsonFactory providesJsonFactory() {
    return new JacksonFactory();
  }

  @Provides @Singleton
  public HttpTransport providesHttpTransport() {
    return new UrlFetchTransport();
  }

  @Provides @Named("projectId") @Singleton
  public String providesProjectId() {
    return AppIdentityServiceFactory.getAppIdentityService().getServiceAccountName().split("@")[0];
  }

  @Provides @Singleton
  public HttpRequestInitializer getRequestInitializer(JsonFactory jsonFactory) {
    SystemProperty.Environment.Value env = SystemProperty.environment.value();
    if (env != SystemProperty.Environment.Value.Production) {
      try {

        return new GoogleCredential.Builder()
            .setTransport(new NetHttpTransport())
            .setJsonFactory(jsonFactory)
            .setServiceAccountId("891347525506-0ktp2j5ll7ifbt766ii64p207q67vjm7@developer.gserviceaccount.com")
            .setServiceAccountScopes(ImmutableList.of("https://www.googleapis.com/auth/calendar"))
            .setServiceAccountPrivateKeyFromP12File(
                new File("/Users/andrew" + File.separator + ".store" + File.separator + "/google_auth.p12"))
            .build();
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
    log.info("Using production credentials for Google APIs");
    return new AppIdentityCredential(ImmutableList.of("https://www.googleapis.com/auth/calendar"));
  }

  @Exposed @Singleton @Provides
  public GcsService providesGoogleCloudStorage() {
    return GcsServiceFactory.createGcsService(
        new RetryParams.Builder()
            .initialRetryDelayMillis(10)
            .retryMaxAttempts(10)
            .totalRetryPeriodMillis(15000)
            .build());
  }

  @Exposed @Singleton @Provides
  public com.google.api.services.calendar.Calendar providesCalendar(HttpTransport httpTransport,
                                                                    JsonFactory jsonFactory,
                                                                    @Named("projectId") String applicationName,
                                                                    HttpRequestInitializer credential) {
    return new com.google.api.services.calendar.Calendar.Builder(
        httpTransport, jsonFactory, credential).setApplicationName(applicationName).build();
  }
}
