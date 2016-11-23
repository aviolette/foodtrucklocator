package foodtruck.server.security;

import com.google.inject.AbstractModule;

/**
 * @author aviolette
 * @since 3/29/15
 */
public class SecurityModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SecurityChecker.class).to(SecurityCheckerImpl.class);
  }
}
