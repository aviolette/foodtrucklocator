package foodtruck.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import foodtruck.dao.appengine.AppEngineDAOModule;
import foodtruck.geolocation.GeolocationModule;
import foodtruck.monitoring.MonitoringModule;
import foodtruck.notifications.NotificationModule;
import foodtruck.schedule.ScheduleModule;
import foodtruck.truckstops.ServiceModule;
import foodtruck.twitter.TwitterModule;
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
        new AppEngineDAOModule(),
        new GeolocationModule(),
        new ServiceModule(),
        new ScheduleModule(),
        new UtilModule(),
        new TwitterModule(),
        new MonitoringModule(),
        new NotificationModule(),
        new FoodtruckServletModule()
    };
  }
}
