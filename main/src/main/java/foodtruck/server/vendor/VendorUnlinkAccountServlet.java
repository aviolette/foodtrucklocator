package foodtruck.server.vendor;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.util.Session;

/**
 * Unlinks a social media accounts credentials from a specific truck.
 *
 * @author aviolette
 * @since 11/8/16
 */
@Singleton
public class VendorUnlinkAccountServlet extends VendorServletSupport {
  @Inject
  public VendorUnlinkAccountServlet(TruckDAO dao, Provider<Session> sessionProvider, UserService userService,
      LocationDAO locationDAO) {
    super(dao, sessionProvider, userService, locationDAO);
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Truck truck) throws ServletException, IOException {
    // TODO: for now I always assume they're unlinking a twitter account
    truck = truck.append()
        .clearTwitterCredentials()
        .build();
    truckDAO.save(truck);
    resp.sendRedirect("/vendor/socialmedia/" + truck.getId());
  }
}
