package foodtruck.server;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.servlet.ServletModule;

import org.joda.time.DateTimeZone;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since Jul 12, 2011
 */
public class FoodtruckServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/cron/recache").with(FoodTruckUpdaterServlet.class);
    serve("/service/*").with(TruckStopServlet.class);
    serveRegex("/[\\w]*").with(FoodTruckServlet.class);
  }

  @Provides
  public DateTimeZone provideDefaultZone() {
    return DateTimeZone.forID("America/Chicago");
  }

  @Provides @Named("center")
  public Location provideMapCenter() {
    return new Location(41.8781136, -87.6297982);
  }
}
