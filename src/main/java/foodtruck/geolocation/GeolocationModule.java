package foodtruck.geolocation;

import com.google.inject.AbstractModule;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class GeolocationModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GeoLocator.class).to(KeywordLocator.class);
  }
}
