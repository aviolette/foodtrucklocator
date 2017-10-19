package foodtruck.monitoring;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * @author aviolette
 * @since 11/30/16
 */
public class CommonMonitoringModule extends AbstractModule {
  @Override
  protected void configure() {
    MonitorInterceptor interceptor = new MonitorInterceptor();
    requestInjection(interceptor);
    bindInterceptor(Matchers.any(),
        Matchers.annotatedWith(Monitored.class), interceptor);
  }
}
