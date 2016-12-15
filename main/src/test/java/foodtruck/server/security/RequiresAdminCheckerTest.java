package foodtruck.server.security;

import javax.ws.rs.WebApplicationException;

import com.google.common.base.Optional;
import com.google.inject.util.Providers;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.user.LoggedInUser;

/**
 * @author aviolette
 * @since 12/15/16
 */
@RunWith(MockitoJUnitRunner.class)
public class RequiresAdminCheckerTest extends Mockito {
  @Mock private MethodInvocation invocation;

  @Test(expected = WebApplicationException.class)
  public void notLoggedIn() throws Throwable {
    RequiresAdminChecker checker = new RequiresAdminChecker();
    checker.setLoggedInUserProvider(Providers.of(Optional.<LoggedInUser>absent()));
    checker.invoke(invocation);
  }

  @Test(expected = WebApplicationException.class)
  public void loggedInButNotAdmin() throws Throwable {
    RequiresAdminChecker checker = new RequiresAdminChecker();
    LoggedInUser user = new LoggedInUser("foo@bar.com", false);
    checker.setLoggedInUserProvider(Providers.of(Optional.of(user)));
    checker.invoke(invocation);
  }

  public void loggedInAdmin() throws Throwable {
    RequiresAdminChecker checker = new RequiresAdminChecker();
    LoggedInUser user = new LoggedInUser("foo@bar.com", true);
    when(invocation.proceed()).thenReturn(null);
    checker.setLoggedInUserProvider(Providers.of(Optional.of(user)));
    checker.invoke(invocation);
    verify(invocation).proceed();
  }
}