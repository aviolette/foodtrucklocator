package foodtruck.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import foodtruck.alexa.AlexaModule;
import foodtruck.book.BookingModule;
import foodtruck.dao.appengine.AppEngineDAOModule;
import foodtruck.dao.memcached.MemcachedModule;
import foodtruck.email.EmailModule;
import foodtruck.geolocation.GeolocationModule;
import foodtruck.googleapi.GoogleApiModule;
import foodtruck.linxup.LinxupModule;
import foodtruck.monitoring.MonitoringModule;
import foodtruck.notifications.NotificationModule;
import foodtruck.schedule.ScheduleModule;
import foodtruck.server.security.SecurityModule;
import foodtruck.socialmedia.SocialMediaModule;
import foodtruck.truckstops.TruckStopsModule;
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
    ImmutableList.Builder<Module> modules = ImmutableList.<Module>builder()
        .add(new AlexaModule())
        .add(new GoogleApiModule())
        .add(new AppEngineDAOModule())
        .add(new MemcachedModule())
        .add(new GeolocationModule())
        .add(new TruckStopsModule())
        .add(new EmailModule())
        .add(new SecurityModule())
        .add(new ScheduleModule())
        .add(new UtilModule())
        .add(new SocialMediaModule())
        .add(new MonitoringModule())
        .add(new NotificationModule())
        .add(new LinxupModule())
        .add(new FoodtruckServletModule());
    if ("true".equals(System.getProperty("foodtrucklocator.supports.booking"))) {
      modules.add(new BookingModule());
    }
    ImmutableList<Module> allModules = modules.build();
    return allModules.toArray(new Module[allModules.size()]);
  }

  // These two methods are overriden to provide the injector to the JSP tags
  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    ServletContext servletContext = servletContextEvent.getServletContext();
    servletContext.removeAttribute(Injector.class.getName());
    super.contextDestroyed(servletContextEvent);
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    Injector injector = getInjector();
    ServletContext servletContext = servletContextEvent.getServletContext();
    servletContext.setAttribute(Injector.class.getName(), injector);
    super.contextInitialized(servletContextEvent);
  }
}
