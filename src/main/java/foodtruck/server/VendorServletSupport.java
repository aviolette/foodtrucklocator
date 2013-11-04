package foodtruck.server;

import java.io.IOException;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 10/15/13
 */
public abstract class VendorServletSupport extends HttpServlet {
  private static final String ERROR_JSP = "/WEB-INF/jsp/vendor/vendorerror.jsp";
  private static final String LANDING_JSP = "/WEB-INF/jsp/vendor/index.jsp";
  protected final TruckDAO truckDAO;

  protected VendorServletSupport(TruckDAO dao) {
    truckDAO = dao;
  }

  @Override protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    UserService userService = UserServiceFactory.getUserService();
    String thisURL = req.getRequestURI();
    final Principal userPrincipal = req.getUserPrincipal();
    if (userPrincipal == null) {
      req.setAttribute("loginUrl", userService.createLoginURL(thisURL));
      req.getRequestDispatcher(LANDING_JSP).forward(req,resp);
      return;
    }
    Set<Truck> trucks = associatedTrucks(req);
    if (trucks.isEmpty()) {
      String logoutUrl = userService.createLogoutURL(thisURL);
      String principal = userPrincipal.getName();
      vendorError("Invalid User",
          MessageFormat.format("The user <strong>{0}</strong> is not associated with any food trucks.  You can " +
              "<a href=\"{1}\">Click Here</a> to sign in as a different user", principal,
              logoutUrl), req, resp);
    } else if (trucks.size() == 1) {
        Truck truck = Iterables.getFirst(trucks, null);
        req.setAttribute("truck", truck);
        dispatchGet(req, resp, truck.getId());
    } else {
        // TODO: implement
        // goto truck selection page
    }
  }

  @Override protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (req.getUserPrincipal() == null) {
      resp.setStatus(401);
      return;
    }
    Set<Truck> trucks = associatedTrucks(req);
    if (trucks.isEmpty()) {
      resp.setStatus(401);
    } else if (trucks.size() == 1) {
      Truck truck = Iterables.getFirst(trucks, null);
      req.setAttribute("truck", truck);
      dispatchPost(req, resp, truck.getId());
    } else {
      // TODO: implement
      // goto truck selection page
    }
  }

  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, String truckId) throws IOException {
    // TODO: default does nothing
  }

  private Set<Truck> associatedTrucks(HttpServletRequest req) {
    if (req.getUserPrincipal() != null) {
      return truckDAO.findByBeaconnaiseEmail(req.getUserPrincipal().getName());
    }
    return ImmutableSet.of();
  }

  protected abstract void dispatchGet(HttpServletRequest req, HttpServletResponse resp, @Nullable String truckId)
      throws ServletException, IOException;

  private void vendorError(String title, String message, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setAttribute("errorTitle", title);
    request.setAttribute("errorMessage", message);
    request.getRequestDispatcher(LANDING_JSP).forward(request, response);
  }
}
