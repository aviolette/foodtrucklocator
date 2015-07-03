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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Provider;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;
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
  private final UserService userService;

  protected VendorServletSupport(TruckDAO dao, Provider<Session> sessionProvider, UserService userService) {
    truckDAO = dao;
    this.sessionProvider = sessionProvider;
    this.userService = userService;
  }

  @Override protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("localFrameworks", "true".equals(System.getProperty("use.local.frameworks", "false")));
    final Principal userPrincipal = getPrincipal(req);
    String thisURL = req.getRequestURI();
    if (userPrincipal == null) {
      log.info("User failed logging in");
      if (thisURL.equals("/vendor")) {
        req = new GuiceHackRequestWrapper(req, LANDING_JSP);
        req.setAttribute("loginUrl", userService.createLoginURL("/vendor"));
        req.getRequestDispatcher(LANDING_JSP).forward(req,resp);
      } else {
        resp.sendRedirect("/vendor");
      }
      return;
    }
    req.setAttribute("logoutUrl", getLogoutUrl(userPrincipal));

    Set<Truck> trucks = associatedTrucks(userPrincipal);
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

  private String getLogoutUrl(Principal userPrincipal) {
    if (isIdentifiedByEmail(userPrincipal)) {
      return userService.createLogoutURL("/vendor");
    } else {
      return "/vendor/logout";
    }
  }


  @Override protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Principal principal = getPrincipal(req);
    if (principal == null) {
      resp.setStatus(401);
      return;
    }
    Set<Truck> trucks = associatedTrucks(principal);
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

  private Set<Truck> associatedTrucks(Principal principal) {
    if (principal != null) {
      if (isIdentifiedByEmail(principal)) {
        return truckDAO.findByBeaconnaiseEmail(principal.getName().toLowerCase());
      } else {
        return ImmutableSet.copyOf(truckDAO.findByTwitterId(principal.getName()));
      }
    }
    return ImmutableSet.of();
  }

  private boolean isIdentifiedByEmail(Principal principal) {
    return principal.getName().contains("@");
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
