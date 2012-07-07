package foodtruck.monitoring;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
public class MonitoringModule extends AbstractModule {
  @Override protected void configure() {
    MonitorInterceptor interceptor = new MonitorInterceptor();
    requestInjection(interceptor);
    bindInterceptor(Matchers.any(),
        Matchers.annotatedWith(Monitored.class), interceptor);
  }
}
