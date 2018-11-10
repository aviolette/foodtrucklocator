package tgc.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import tgc.db.DBModule;

/**
 * @author aviolette
 * @since 11/7/12
 */
public class ServerConfig extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(modules());
  }

  private Module[] modules() {
    return new Module[] {
        new ServerModule(), new DBModule()
    };
  }
}
