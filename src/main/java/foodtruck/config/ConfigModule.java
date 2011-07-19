package foodtruck.config;

import com.google.inject.AbstractModule;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class ConfigModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TruckConfigParser.class).to(TruckConfigParserImpl.class);
  }
}
