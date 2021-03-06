package foodtruck.geolocation;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import foodtruck.annotations.Google;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class GeolocationModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GeoLocator.class).to(CacheAndForwardLocator.class);
    bind(GeoLocator.class).annotatedWith(Google.class).to(GoogleGeolocator.class);
  }

  @Provides
  @Named("foodtrucklocator.state")
  public String providesState() {
    return System.getProperty("foodtrucklocator.state", "IL");
  }

  @GoogleEndPoint @Provides @Singleton
  public WebResource provideWebResource(Client client) {
    return client.resource("https://maps.googleapis.com/maps/api/geocode/json");
  }
}
