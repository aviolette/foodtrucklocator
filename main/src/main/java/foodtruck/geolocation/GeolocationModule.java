package foodtruck.geolocation;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class GeolocationModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GeoLocator.class).to(CacheAndForwardLocator.class);
    bind(GeoLocator.class).annotatedWith(SecondaryGeolocator.class).to(OrderedGeolocator.class);
  }

  @Provides @GoogleServerApiKey
  public Optional<String> provideServerKey() {
    String key = System.getProperty("foodtrucklocator.google.api.key", null);
    if (Strings.isNullOrEmpty(key)) {
      return Optional.absent();
    }
    return Optional.of(key);
  }

  @Provides
  @Named("foodtrucklocator.state")
  public String providesState() {
    return System.getProperty("foodtrucklocator.state", "IL");
  }

  @GoogleEndPoint @Provides @Singleton
  public WebResource provideWebResource() {
    return Client.create().resource("https://maps.googleapis.com/maps/api/geocode/json");
  }

  @YQLEndPoint @Provides @Singleton
  public WebResource provideYQLWebResource() {
    return Client.create().resource("http://query.yahooapis.com/v1/public/yql");
  }
}
