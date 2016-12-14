package foodtruck.server.security;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import foodtruck.server.resources.ErrorPayload;

/**
 * @author aviolette
 * @since 12/13/16
 */
class RequiresAdminChecker implements MethodInterceptor {
  private static final Logger log = Logger.getLogger(RequiresAdminChecker.class.getName());

  @Inject
  public RequiresAdminChecker() {
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    UserService service = UserServiceFactory.getUserService();
    log.log(Level.INFO, "Checking for admin privileges {0} {1}",
        new Object[]{service.getCurrentUser(), invocation.getMethod().getName()});
    if (!service.isUserLoggedIn() || !service.isUserAdmin()) {
      Response response = Response.status(Response.Status.FORBIDDEN)
          .type(MediaType.APPLICATION_JSON_TYPE)
          .entity(new ErrorPayload("fobidden"))
          .build();
      throw new WebApplicationException(response);
    }
    return invocation.proceed();
  }
}
