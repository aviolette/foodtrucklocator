package foodtruck.server.vendor;

import java.io.IOException;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Provider;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.util.Session;

/**
 * @author aviolette
 * @since 10/15/13
 */
public abstract class VendorServletSupport extends HttpServlet {
  private static final Logger log = Logger.getLogger(VendorServletSupport.class.getName());
  private static final String LANDING_JSP = "/WEB-INF/jsp/vendor/index.jsp";
  protected final TruckDAO truckDAO;
  protected final Provider<Session> sessionProvider;

  protected VendorServletSupport(TruckDAO dao, Provider<Session> sessionProvider) {
    truckDAO = dao;
    this.sessionProvider = sessionProvider;
  }

  @Override protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("localFrameworks", "true".equals(System.getProperty("use.local.frameworks", "false")));
    UserService userService = UserServiceFactory.getUserService();
    String thisURL = req.getRequestURI();
    final Principal userPrincipal = getPrincipal(req);
    if (userPrincipal == null) {
      log.info("User failed logging in");
      req.setAttribute("loginUrl", userService.createLoginURL(thisURL));
      req.getRequestDispatcher(LANDING_JSP).forward(req,resp);
      return;
    }
    Set<Truck> trucks = associatedTrucks(req);
    if (trucks.isEmpty()) {
      String logoutUrl = userService.createLogoutURL(thisURL);
      String principal = userPrincipal.getName();
      final String message = MessageFormat
          .format("The user <strong>{0}</strong> is not associated with any food trucks.  You can " +
              "<a href=\"{1}\">Click Here</a> to sign in as a different user", principal,
              logoutUrl);
      vendorError("Invalid User", message, req, resp);
      log.info("Sent this message to the user" + message);
    } else if (trucks.size() == 1) {
        Truck truck = Iterables.getFirst(trucks, null);
        req.setAttribute("truck", truck);
        dispatchGet(req, resp, truck);
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
      log.info("Sent 401 on post because user didn't belong to any trucks");
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
    Principal principal = getPrincipal(req);
    if (principal != null) {
      if (principal.getName().contains("@")) {
        return truckDAO.findByBeaconnaiseEmail(principal.getName().toLowerCase());
      } else {
        return ImmutableSet.copyOf(truckDAO.findByTwitterId(principal.getName()));
      }
    }
    return ImmutableSet.of();
  }

  protected abstract void dispatchGet(HttpServletRequest req, HttpServletResponse resp, @Nullable Truck truck)
      throws ServletException, IOException;

  private void vendorError(String title, String message, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setAttribute("errorTitle", title);
    request.setAttribute("errorMessage", message);
    request.getRequestDispatcher(LANDING_JSP).forward(request, response);
  }

  protected @Nullable Principal getPrincipal(HttpServletRequest request) {
    Session session = sessionProvider.get();
    Principal principal = (Principal)session.getProperty("principal");
    return principal == null ? request.getUserPrincipal() : principal;
  }
}
