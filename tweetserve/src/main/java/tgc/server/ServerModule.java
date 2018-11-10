package tgc.server;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * @author aviolette
 * @since 11/7/12
 */
public class ServerModule extends ServletModule {
  @Override
  protected void configureServlets() {
    serve("/services/*").with(GuiceContainer.class,
        ImmutableMap.of(PackagesResourceConfig.PROPERTY_PACKAGES, "tgc.server.resources"));
  }
}
