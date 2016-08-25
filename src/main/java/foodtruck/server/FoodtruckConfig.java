package foodtruck.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import foodtruck.alexa.AlexaModule;
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
    return new Module[] {new AlexaModule(),
        new GoogleApiModule(),
        new AppEngineDAOModule(),
        new MemcachedModule(),
        new GeolocationModule(),
        new ServiceModule(),
        new EmailModule(),
        new SecurityModule(),
        new ScheduleModule(),
        new UtilModule(),
        new SocialMediaModule(),
        new MonitoringModule(),
        new NotificationModule(),
        new LinxupModule(),
        new FoodtruckServletModule()
    };
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
