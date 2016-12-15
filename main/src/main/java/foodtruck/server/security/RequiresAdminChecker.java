package foodtruck.server.security;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import foodtruck.server.resources.ErrorPayload;
import foodtruck.user.LoggedInUser;

/**
 * @author aviolette
 * @since 12/13/16
 */
class RequiresAdminChecker implements MethodInterceptor {
  private static final Logger log = Logger.getLogger(RequiresAdminChecker.class.getName());
  private Provider<Optional<LoggedInUser>> loggedInUserProvider;

  @Inject
  public void setLoggedInUserProvider(Provider<Optional<LoggedInUser>> loggedInUserProvider) {
    this.loggedInUserProvider = loggedInUserProvider;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    Optional<LoggedInUser> loggedInUser = loggedInUserProvider.get();
    if (!loggedInUser.isPresent()) {
      log.log(Level.WARNING, "User is not logged in but requesting admin resource");
      forbidden();
    }
    LoggedInUser user = loggedInUser.get();
    log.log(Level.INFO, "Checking for admin privileges {0}", user);
    if (!user.isAdmin()) {
      forbidden();
    }
    return invocation.proceed();
  }

  void forbidden() {
    Response response = Response.status(Response.Status.FORBIDDEN)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity(new ErrorPayload("fobidden"))
        .build();
    throw new WebApplicationException(response);
  }
}
