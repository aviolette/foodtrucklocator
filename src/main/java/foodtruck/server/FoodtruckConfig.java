package foodtruck.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import foodtruck.config.ConfigModule;
import foodtruck.dao.appengine.AppEngineDAOModule;
import foodtruck.geolocation.GeolocationModule;
import foodtruck.schedule.ScheduleModule;
import foodtruck.truckstops.ServiceModule;
import foodtruck.util.UtilModule;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class FoodtruckConfig extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(modules());
  }

  private Module[] modules() {
    return new Module[] {
        new ConfigModule(),
        new AppEngineDAOModule(),
        new GeolocationModule(),
        new ServiceModule(),
        new ScheduleModule(),
        new UtilModule(),
        new FoodtruckServletModule()
    };
  }
}
