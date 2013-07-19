package foodtruck.server;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 7/12/13
 */
@Singleton
public class BeaconnaiseServlet extends HttpServlet {
  private final TruckDAO truckDAO;
  private static final String JSP = "/WEB-INF/jsp/vendor/beaconnaise.jsp";
  private static final String ERROR_JSP = "/WEB-INF/jsp/vendor/vendorerror.jsp";
  private static final String LANDING_JSP = "/WEB-INF/jsp/vendor/index.jsp";

  @Inject
  public BeaconnaiseServlet(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    UserService userService = UserServiceFactory.getUserService();
    String thisURL = req.getRequestURI();
    if (req.getUserPrincipal() != null) {
      String principal = req.getUserPrincipal().getName();
      Set<Truck> trucks = truckDAO.findByBeaconnaiseEmail(req.getUserPrincipal().getName());
      if (trucks.isEmpty()) {
        String logoutUrl = userService.createLogoutURL(thisURL);
        vendorError("Invalid User",
            MessageFormat.format("The user <strong>{0}</strong> is not associated with any food trucks.  You can " +
            "<a href=\"{1}\">Click Here</a> to sign in as a different user", principal,
            logoutUrl), req, resp);
        // goto no trucks page
      } else if (trucks.size() == 1) {
        req.setAttribute("truck", Iterables.getFirst(trucks, null));
        req.getRequestDispatcher(JSP).forward(req, resp);
      } else {
        // goto truck selection page
      }
    } else {
      req.setAttribute("loginUrl", userService.createLoginURL(thisURL));
      req.getRequestDispatcher(LANDING_JSP).forward(req,resp);
    }
  }

  private void vendorError(String title, String message, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setAttribute("errorTitle", title);
    request.setAttribute("errorMessage", message);
    request.getRequestDispatcher(LANDING_JSP).forward(request, response);
  }
}
