package foodtruck.server.security;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * @author aviolette
 * @since 3/29/15
 */
public class SecurityModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SecurityChecker.class).to(SecurityCheckerImpl.class);
  }

  @Provides
  public UserService provideUserService() {
    return UserServiceFactory.getUserService();
  }
}
