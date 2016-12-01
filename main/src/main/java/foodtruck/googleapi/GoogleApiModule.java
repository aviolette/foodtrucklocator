package foodtruck.googleapi;

import javax.inject.Named;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 11/20/14
 */
public class GoogleApiModule extends AbstractModule {
  @Override
  protected void configure() {
  }

  @Singleton
  @Provides
  public Calendar providesCalendar(HttpTransport httpTransport, JsonFactory jsonFactory,
      @Named("projectId") String applicationName, HttpRequestInitializer credential) {
    return new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName(applicationName)
        .build();
  }
}
