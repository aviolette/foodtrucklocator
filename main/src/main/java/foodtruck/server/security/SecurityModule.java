package foodtruck.server.security;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import foodtruck.annotations.RequiresAdmin;
import foodtruck.annotations.RequiresAppKeyWithCountRestriction;

/**
 * @author aviolette
 * @since 3/29/15
 */
public class SecurityModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SecurityChecker.class).to(SecurityCheckerImpl.class);
    RequiresAdminChecker adminChecker = new RequiresAdminChecker();
    requestInjection(adminChecker);
    bindInterceptor(Matchers.any(), Matchers.annotatedWith(RequiresAdmin.class), adminChecker);
    RequiresAppKeyWithCountRestrictionChecker checker = new RequiresAppKeyWithCountRestrictionChecker();
    requestInjection(checker);
    bindInterceptor(Matchers.any(), Matchers.annotatedWith(RequiresAppKeyWithCountRestriction.class), checker);
  }
}
