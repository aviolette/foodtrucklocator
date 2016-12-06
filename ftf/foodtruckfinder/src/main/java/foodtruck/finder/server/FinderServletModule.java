package foodtruck.finder.server;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.api.core.PackagesResourceConfig;

/**
 * @author aviolette
 * @since 10/12/16
 */
public class FinderServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    serve("/v3/services/*").with(GuiceContainer.class,
        ImmutableMap.of(PackagesResourceConfig.PROPERTY_PACKAGES, "foodtruck.finder.server.services.v3"));
  }
}
