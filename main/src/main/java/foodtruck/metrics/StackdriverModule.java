package foodtruck.metrics;

import com.google.inject.AbstractModule;

/**
 * @author aviolette
 * @since 10/8/18
 */
public class StackdriverModule extends AbstractModule {

  @Override
  protected void configure() {
      bind(MetricsService.class).to(StackDriverMetricsService.class);
  }
}
