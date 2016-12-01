package foodtruck.finder;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import foodtruck.finder.server.FinderServletModule;
import foodtruck.time.TimeModule;

/**
 * @author aviolette
 * @since 10/12/16
 */
public class Config extends GuiceServletContextListener {
  protected Injector getInjector() {
    return Guice.createInjector(modules());
  }

  private Module[] modules() {
    return new Module[] {new FinderServletModule(), new TimeModule()};
  }
}
