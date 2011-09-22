package foodtruck.geolocation;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class GeolocationModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GeoLocator.class).to(CacheAndStoreLocator.class);
    bind(GeoLocator.class).annotatedWith(SecondaryGeolocator.class).to(GoogleGeolocator.class);
  }

  @GeoLocation @Provides @Singleton
  public WebResource provideWebResource() {
    Client c = Client.create();
    return c.resource("http://maps.googleapis.com/maps/api/geocode/json");
  }
}
