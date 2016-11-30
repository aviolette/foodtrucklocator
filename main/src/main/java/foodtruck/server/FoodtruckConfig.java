package foodtruck.server;

import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import foodtruck.alexa.AlexaModule;
import foodtruck.appengine.AppengineModule;
import foodtruck.book.BookingModule;
import foodtruck.dao.appengine.AppEngineDAOModule;
import foodtruck.dao.memcached.MemcachedModule;
import foodtruck.geolocation.GeolocationModule;
import foodtruck.googleapi.GoogleApiModule;
import foodtruck.linxup.LinxupModule;
import foodtruck.monitoring.MonitoringModule;
import foodtruck.notifications.NotificationModule;
import foodtruck.schedule.ScheduleModule;
import foodtruck.server.security.SecurityModule;
import foodtruck.socialmedia.SocialMediaModule;
import foodtruck.time.TimeModule;
import foodtruck.truckstops.TruckStopsModule;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class FoodtruckConfig extends GuiceServletContextListener {
  private static final Logger log = Logger.getLogger(FoodtruckConfig.class.getName());

  @Override
  protected Injector getInjector() {
    log.info("Loading injector");
    return Guice.createInjector(modules());
  }

  private Module[] modules() {
    ImmutableList.Builder<Module> modules = ImmutableList.<Module>builder().add(new AppengineModule())
        .add(new AlexaModule())
        .add(new GoogleApiModule())
        .add(new AppEngineDAOModule())
        .add(new MemcachedModule())
        .add(new GeolocationModule())
        .add(new TruckStopsModule())
        .add(new SecurityModule())
        .add(new ScheduleModule())
        .add(new SocialMediaModule())
        .add(new MonitoringModule())
        .add(new NotificationModule())
        .add(new LinxupModule())
        .add(new TimeModule())
        .add(new FoodtruckServletModule());
    if ("true".equals(System.getProperty("foodtrucklocator.supports.booking"))) {
      modules.add(new BookingModule());
    }
    ImmutableList<Module> allModules = modules.build();
    return allModules.toArray(new Module[allModules.size()]);
  }
}
