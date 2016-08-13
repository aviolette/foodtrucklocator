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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Provider;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
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
  private final LocationDAO locationDAO;

  VendorServletSupport(TruckDAO dao, Provider<Session> sessionProvider, UserService userService,
      LocationDAO locationDAO) {
    truckDAO = dao;
    this.sessionProvider = sessionProvider;
    this.userService = userService;
    this.locationDAO = locationDAO;
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
        req.setAttribute("loginUrl", userService.createLoginURL("/vendor?check"));
        req.getRequestDispatcher(LANDING_JSP).forward(req,resp);
      } else {
        resp.sendRedirect("/vendor");
      }
      return;
    }
    Set<Truck> trucks = associatedTrucks(userPrincipal);
    if (trucks.isEmpty()) {
      Set<Location> locations = associatedLocations(userPrincipal);
      if (locations.isEmpty()) {
        gotoLogonPage(req, resp, userPrincipal, thisURL);
      } else {
        req.setAttribute("logoutUrl", getLogoutUrl(userPrincipal));
        Location location =  Iterables.getFirst(locations, null);
        req.setAttribute("location", location);
        @SuppressWarnings("ConstantConditions")
        String locationUrl = "/vendor/locations/"+location.getKey();
        if (!thisURL.startsWith(locationUrl)) {
          resp.sendRedirect(locationUrl);
        } else {
          req.setAttribute("locationId", location.getKey());
          dispatchGet(req, resp, location);
        }
      }
    } else if (trucks.size() == 1) {
      req.setAttribute("logoutUrl", getLogoutUrl(userPrincipal));
      Truck truck = Iterables.getFirst(trucks, null);
      req.setAttribute("truck", truck);
      req.setAttribute("vendorIconUrl", truck.getPreviewIconUrl());
      req.setAttribute("vendorIconDescription", truck.getName());
      log.log(Level.INFO, "User {0}", userPrincipal.getName());
      dispatchGet(req, resp, truck);
    }
    // TODO implement multiple trucks and multiple locations associated with a user account
  }

  private void gotoLogonPage(HttpServletRequest req, HttpServletResponse resp, Principal userPrincipal,
      String thisURL) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, LANDING_JSP);
    Session session = sessionProvider.get();
    session.invalidate();
    if (thisURL.equals("/vendor") && (req.getParameter("check") != null || !isIdentifiedByEmail(userPrincipal))) {
      String logoutUrl = userService.createLogoutURL(thisURL);
      String principal = userPrincipal.getName();
      final String message = MessageFormat
          .format("The user <strong>{0}</strong> is not associated with any food trucks.", principal, logoutUrl);
      vendorError("Invalid User", message, req, resp);
      log.info("Sent this message to the user" + message);
    } else {
      req.setAttribute("loginUrl", userService.createLoginURL("/vendor?check"));
      req.getRequestDispatcher(LANDING_JSP).forward(req,resp);
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
      Set<Location> locations = associatedLocations(principal);
      if (locations.isEmpty()) {
        log.info("Sent 401 on post because user didn't belong to any trucks");
        resp.setStatus(401);
      } else {
        Location location =  Iterables.getFirst(locations, null);
        req.setAttribute("location", location);
        @SuppressWarnings("ConstantConditions")
        String locationUrl = "/vendor/locations/"+location.getKey();
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
      dispatchPost(req, resp, truck.getId());
    }
  }

  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, Location location, String principalName) throws IOException {
  }

  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, String truckId) throws IOException {
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

  private Set<Location> associatedLocations(Principal principal) {
    if (isIdentifiedByEmail(principal)) {
      return ImmutableSet.copyOf(locationDAO.findByManagerEmail(principal.getName()));
    } else {
      return ImmutableSet.copyOf(locationDAO.findByTwitterId(principal.getName()));
    }
  }

  private boolean isIdentifiedByEmail(Principal principal) {
    return principal.getName().contains("@");
  }

  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp, @Nullable Truck truck)
      throws ServletException, IOException {
    throw new UnsupportedOperationException("dispatchGet(truck)");

  }

  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp, @Nullable Location location)
      throws ServletException, IOException {
    throw new UnsupportedOperationException("dispatchGet(location)");
  }

  private void vendorError(String title, String message, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setAttribute("errorTitle", title);
    request.setAttribute("errorMessage", message);
    request.getRequestDispatcher(LANDING_JSP).forward(request, response);
  }

  @Nullable
  private Principal getPrincipal(HttpServletRequest request) {
    Session session = sessionProvider.get();
    Principal principal = (Principal)session.getProperty("principal");
    return principal == null ? request.getUserPrincipal() : principal;
  }
}
