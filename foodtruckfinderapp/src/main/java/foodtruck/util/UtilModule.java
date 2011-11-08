package foodtruck.util;

import com.google.inject.AbstractModule;

/**
 * @author aviolette@gmail.com
 * @since 9/22/11
 */
public class UtilModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Clock.class).to(ClockImpl.class);
  }
}
