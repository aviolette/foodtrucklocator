package foodtruck.google;

import java.io.IOException;
import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import foodtruck.annotations.GoogleJavascriptApiKey;
import foodtruck.annotations.GoogleServerAPIKey;
import foodtruck.server.security.PropertyStore;

public class GoogleModule extends AbstractModule {

  private static final String GOOGLE_PROPERTIES = "google.properties";

  @Singleton
  @Provides
  @Named(GOOGLE_PROPERTIES)
  public Properties providesProperties(PropertyStore propertyStore) throws IOException {
    return propertyStore.findProperties("google");
  }

  @Provides
  @GoogleJavascriptApiKey
  public String providesGoogleJavascriptKey(@Named(GOOGLE_PROPERTIES) Properties properties) {
    return properties.getProperty("foodtrucklocator.google.javascript.api.key");
  }

  @Provides
  @GoogleServerAPIKey
  public String providesGoogleServerKey(@Named(GOOGLE_PROPERTIES) Properties properties) {
    return properties.getProperty("foodtrucklocator.google.api.key");
  }
}
