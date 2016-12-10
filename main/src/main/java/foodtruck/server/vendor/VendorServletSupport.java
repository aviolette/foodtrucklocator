package foodtruck.server.vendor;

import java.io.IOException;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.common.collect.Iterables;
import com.google.inject.Provider;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 10/15/13
 */
public abstract class VendorServletSupport extends HttpServlet {
  private static final Logger log = Logger.getLogger(VendorServletSupport.class.getName());
  private static final String LANDING_JSP = "/WEB-INF/jsp/vendor/index.jsp";
  protected final TruckDAO truckDAO;
  private final UserService userService;
  private final Provider<SessionUser> sessionUserProvider;

  VendorServletSupport(TruckDAO dao, UserService userService, Provider<SessionUser> sessionUserProvider) {
    truckDAO = dao;
    this.userService = userService;
    this.sessionUserProvider = sessionUserProvider;
  }

  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    SessionUser sessionUser = sessionUserProvider.get();
    String thisURL = req.getRequestURI();
    if (!sessionUser.isLoggedIn()) {
      log.info("User failed logging in");
      if (thisURL.equals("/vendor")) {
        req = new GuiceHackRequestWrapper(req, LANDING_JSP);
        req.setAttribute("loginUrl", userService.createLoginURL("/vendor?check"));
        req.getRequestDispatcher(LANDING_JSP)
            .forward(req, resp);
      } else {
        resp.sendRedirect("/vendor");
      }
      return;
    }
    Set<Truck> trucks = sessionUser.associatedTrucks();
    if (trucks.isEmpty()) {
      Set<Location> locations = sessionUser.associatedLocations();
      if (locations.isEmpty()) {
        gotoLogonPage(req, resp, thisURL);
      } else {
        req.setAttribute("logoutUrl", getLogoutUrl());
        Location location = Iterables.getFirst(locations, null);
        req.setAttribute("location", location);
        @SuppressWarnings("ConstantConditions") String locationUrl = "/vendor/locations/" + location.getKey();
        if (!thisURL.startsWith(locationUrl)) {
          resp.sendRedirect(locationUrl);
        } else {
          req.setAttribute("locationId", location.getKey());
          dispatchGet(req, resp, location);
        }
      }
    } else {
      req.setAttribute("logoutUrl", getLogoutUrl());
      Truck truck = Iterables.getFirst(trucks, null);
      req.setAttribute("truck", truck);
      req.setAttribute("vendorIconUrl", truck.getPreviewIconUrl());
      req.setAttribute("vendorIconDescription", truck.getName());
      log.log(Level.INFO, "User {0}", sessionUser);
      dispatchGet(req, resp, truck);
    }

    if (trucks.size() > 1) {
      log.log(Level.SEVERE, "Multiple trucks returned for {0}. Using first one {1}",
          new Object[]{sessionUser.getPrincipal().getName(), trucks});
    }
    // TODO implement multiple trucks and multiple locations associated with a user account
  }

  private void gotoLogonPage(HttpServletRequest req, HttpServletResponse resp, String thisURL) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, LANDING_JSP);
    SessionUser sessionUser = sessionUserProvider.get();
    sessionUser.invalidate();
    if (thisURL.equals("/vendor") && (req.getParameter("check") != null || !sessionUser.isIdentifiedByEmail())) {
      String logoutUrl = userService.createLogoutURL(thisURL);
      final String message = MessageFormat.format(
          "The user <strong>{0}</strong> is not associated with any food trucks.", sessionUser, logoutUrl);
      vendorError("Invalid User", message, req, resp);
      log.info("Sent this message to the user" + message);
    } else {
      req.setAttribute("loginUrl", userService.createLoginURL("/vendor?check"));
      req.getRequestDispatcher(LANDING_JSP)
          .forward(req, resp);
    }
  }

  private String getLogoutUrl() {
    if (sessionUserProvider.get()
        .isIdentifiedByEmail()) {
      return userService.createLogoutURL("/vendor");
    } else {
      return "/vendor/logout";
    }
  }

  @Override
  protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    SessionUser sessionUser = sessionUserProvider.get();
    Principal principal = sessionUser.getPrincipal();
    if (principal == null) {
      resp.setStatus(401);
      return;
    }
    Set<Truck> trucks = sessionUser.associatedTrucks();
    if (trucks.isEmpty()) {
      Set<Location> locations = sessionUser.associatedLocations();
      if (locations.isEmpty()) {
        log.info("Sent 401 on post because user didn't belong to any trucks");
        resp.setStatus(401);
      } else {
        Location location = Iterables.getFirst(locations, null);
        req.setAttribute("location", location);
        @SuppressWarnings("ConstantConditions") String locationUrl = "/vendor/locations/" + location.getKey();
        String thisURL = req.getRequestURI();
        if (!thisURL.startsWith(locationUrl)) {
          resp.setStatus(401);
        } else {
          dispatchPost(req, resp, location, principal.getName());
        }
      }
    } else if (trucks.size() == 1) {
      Truck truck = Iterables.getFirst(trucks, null);
      //noinspection ConstantConditions
      req.setAttribute("truck", truckDAO.findById(truck.getId()));
      log.log(Level.INFO, "User {0}", principal.getName());
      dispatchPost(req, resp, truck.getId(), principal);
    }
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    SessionUser sessionUser = sessionUserProvider.get();
    Principal principal = sessionUser.getPrincipal();
    if (principal == null) {
      resp.setStatus(401);
      return;
    }
    Set<Truck> trucks = sessionUser.associatedTrucks();
    if (trucks.size() == 1) {
      Truck truck = Iterables.getFirst(trucks, null);
      //noinspection ConstantConditions
      req.setAttribute("truck", truckDAO.findById(truck.getId()));
      log.log(Level.INFO, "User {0}", principal.getName());
      dispatchPut(req, resp, truck);
    }
  }

  protected void dispatchPut(HttpServletRequest req, HttpServletResponse resp, Truck truck) throws IOException {
  }

  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, Location location,
      String principalName) throws IOException {
  }

  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, String truckId, Principal principal) throws IOException {
  }

  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Truck truck) throws ServletException, IOException {
    throw new UnsupportedOperationException(getClass().getName() + ".dispatchGet(truck)");

  }

  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Location location) throws ServletException, IOException {
    throw new UnsupportedOperationException(getClass().getName() + "dispatchGet(location)");
  }

  private void vendorError(String title, String message, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    request.setAttribute("errorTitle", title);
    request.setAttribute("errorMessage", message);
    request.getRequestDispatcher(LANDING_JSP)
        .forward(request, response);
  }

  protected void flash(String message, HttpServletResponse resp) {
    resp.setHeader("Set-Cookie", "flash=" + message + ";Max-Age=30000");
  }
}
