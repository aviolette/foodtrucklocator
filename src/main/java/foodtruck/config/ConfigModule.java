package foodtruck.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class ConfigModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TruckConfigParser.class).to(TruckConfigParserImpl.class);
  }

  @Provides @Named("configDate") @Singleton
  public DateTimeFormatter provideFormatter(DateTimeZone zone) {
    return DateTimeFormat.forPattern("MM/dd/YYYY").withZone(zone);
  }
}
