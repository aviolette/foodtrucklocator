package foodtruck.server.security;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.resources.ErrorPayload;

/**
 * @author aviolette
 * @since 3/29/15
 */
public class SecurityCheckerImpl implements SecurityChecker {
  private final Provider<UserService> userServiceProvider;
  private final TruckDAO truckDAO;

  @Inject
  public SecurityCheckerImpl(Provider<UserService> userServiceProvider, TruckDAO truckDAO) {
    this.userServiceProvider = userServiceProvider;
    this.truckDAO = truckDAO;
  }

  @Override
  public void requiresAdmin() {
    UserService userService = userServiceProvider.get();
    if (!userService.isUserLoggedIn() || !userService.isUserAdmin()) {
      Response response = Response.status(Response.Status.FORBIDDEN)
          .type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorPayload("fobidden"))
          .build();
      throw new WebApplicationException(response);
    }
  }

  @Override
  public void requiresLoggedInAs(String truckId) {
    UserService userService = userServiceProvider.get();
    if (userService.isUserAdmin()) {
      return;
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
}
