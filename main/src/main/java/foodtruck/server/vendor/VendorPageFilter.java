package foodtruck.server.vendor;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.http.HttpStatusCodes;
import com.google.appengine.api.users.UserService;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 12/12/16
 */
@Singleton
public class VendorPageFilter implements Filter {
  static final String TRUCK = "truck";
  static final String PRINCIPAL = "principal";
  private static final Logger log = Logger.getLogger(VendorPageFilter.class.getName());
  private static final String LANDING_JSP = "/WEB-INF/jsp/vendor/index.jsp";
  private final UserService userService;
  private final Provider<SessionUser> sessionUserProvider;

  @Inject
  public VendorPageFilter(UserService userService, Provider<SessionUser> sessionUserProvider) {
    this.userService = userService;
    this.sessionUserProvider = sessionUserProvider;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) servletRequest;
    HttpServletResponse resp = (HttpServletResponse) servletResponse;
    String thisURL = req.getRequestURI();
    if (thisURL.startsWith("/vendor/twitter") || thisURL.startsWith("/vendor/callback")) {
      filterChain.doFilter(req, resp);
      return;
    }
    SessionUser sessionUser = sessionUserProvider.get();
    if (!sessionUser.isLoggedIn()) {
      log.info("User failed logging in");
      if (req.getMethod()
          .equalsIgnoreCase("get")) {
        loginFailed(req, resp, thisURL);
      } else {
        resp.sendError(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED);
      }
      return;
    }
    Set<Truck> trucks = sessionUser.associatedTrucks();
    if (trucks.isEmpty()) {
      if (req.getMethod()
          .equalsIgnoreCase("get")) {
        notAssociatedWithTruck(req, resp, thisURL);
      } else {
        resp.sendError(HttpStatusCodes.STATUS_CODE_FORBIDDEN);
      }
    } else {
      req.setAttribute("logoutUrl", getLogoutUrl());
      Truck truck = Iterables.getFirst(trucks, null);
      req.setAttribute(TRUCK, truck);
      req.setAttribute(PRINCIPAL, sessionUser.getPrincipal());
      req.setAttribute("vendorIconUrl", truck.getPreviewIconUrl());
      req.setAttribute("vendorIconDescription", truck.getName());
      log.log(Level.INFO, "Vendor dashboard request from {0}", sessionUser);
      filterChain.doFilter(req, resp);
    }
    if (trucks.size() > 1) {
      // TODO implement multiple trucks and multiple locations associated with a user account
      log.log(Level.SEVERE, "Multiple trucks returned for {0}. Using first one {1}",
          new Object[]{sessionUser.getPrincipal().getName(), trucks});
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

  void loginFailed(HttpServletRequest req, HttpServletResponse resp,
      String thisURL) throws ServletException, IOException {
    if (thisURL.equals("/vendor")) {
      req = new GuiceHackRequestWrapper(req, LANDING_JSP);
      req.setAttribute("loginUrl", userService.createLoginURL("/vendor?check"));
      req.getRequestDispatcher(LANDING_JSP)
          .forward(req, resp);
    } else {
      resp.sendRedirect("/vendor");
    }
  }

  private void notAssociatedWithTruck(HttpServletRequest req, HttpServletResponse resp,
      String thisURL) throws ServletException, IOException {
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

  private void vendorError(String title, String message, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    request.setAttribute("errorTitle", title);
    request.setAttribute("errorMessage", message);
    request.getRequestDispatcher(LANDING_JSP)
        .forward(request, response);
  }

  @Override
  public void destroy() {
  }
}
