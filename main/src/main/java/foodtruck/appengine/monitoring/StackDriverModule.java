package foodtruck.appengine.monitoring;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import foodtruck.monitoring.CounterPublisher;
import foodtruck.monitoring.StackDriver;

/**
 * @author aviolette
 * @since 10/8/18
 */
public class StackDriverModule extends AbstractModule {

  @Override
  protected void configure() {
  }


  @Provides @StackDriver
  public CounterPublisher providesCounterPublisher(StackDriverPublisher publisher) {
    return publisher;
  }
}
