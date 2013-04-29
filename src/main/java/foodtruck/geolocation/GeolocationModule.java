package foodtruck.geolocation;

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
    bind(GeoLocator.class).to(CacheAndStoreLocator.class);
    bind(GeoLocator.class).annotatedWith(SecondaryGeolocator.class).to(OrderedGeolocator.class);
  }

  @Provides @Named("yahoo.app.id") @Singleton
  public String provideYahooAppId() {
    return System.getProperty("yahoo.app.id");
  }

  @GoogleEndPoint @Provides @Singleton
  public WebResource provideWebResource() {
    Client c = Client.create();
    return c.resource("http://maps.googleapis.com/maps/api/geocode/json");
  }

  @YahooEndPoint @Provides @Singleton
  public WebResource provideYahooWebResource() {
    Client c = Client.create();
    return c.resource("http://where.yahooapis.com/geocode");
  }

  @YQLEndPoint @Provides @Singleton
  public WebResource provideYQLWebResource() {
    Client c = Client.create();
    return c.resource("http://query.yahooapis.com/v1/public/yql");
  }

}
