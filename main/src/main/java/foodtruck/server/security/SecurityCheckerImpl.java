package foodtruck.server.security;

import java.security.Principal;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.resources.ErrorPayload;
import foodtruck.session.Session;

/**
 * @author aviolette
 * @since 3/29/15
 */
public class SecurityCheckerImpl implements SecurityChecker {
  private final Provider<UserService> userServiceProvider;
  private final TruckDAO truckDAO;
  private final Provider<Session> sessionProvider;

  @Inject
  public SecurityCheckerImpl(Provider<Session> sessionProvider,
      Provider<UserService> userServiceProvider, TruckDAO truckDAO) {
    this.userServiceProvider = userServiceProvider;
    this.truckDAO = truckDAO;
    this.sessionProvider = sessionProvider;
  }

  @Override
  public void requiresLoggedInAs(String truckId) {
    UserService userService = userServiceProvider.get();
    if (userService.isUserLoggedIn() && userService.isUserAdmin()) {
      return;
    }
    Session session = sessionProvider.get();
    Principal principal = (Principal) session.getProperty("principal");
    if (principal != null) {
      // twitter user
      for (Truck truck : truckDAO.findByTwitterId(principal.getName())) {
        if (truck.getId().equals(truckId)) {
          return;
        }
      }
    }

    if (userService.isUserLoggedIn()) {
      User user = userService.getCurrentUser();
      for (Truck truck : truckDAO.findByBeaconnaiseEmail(user.getEmail().toLowerCase())) {
        if (truck.getId().equals(truckId)) {
          return;
        }
      }
    }
    Response response = Response.status(Response.Status.FORBIDDEN)
        .type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorPayload("fobidden"))
        .build();
    throw new WebApplicationException(response);
  }

  @Override
  public boolean isAdmin() {
    return userServiceProvider.get().isUserAdmin();
  }

  @Override
  public void requiresSecret(@Nullable String secret) throws WebApplicationException {
    if (Strings.isNullOrEmpty(secret)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    if (System.getenv().get("FOODTRUCK_API_SECRET").equals(secret)) {
      return;
    }
    throw new WebApplicationException(Response.Status.FORBIDDEN);
  }
}
