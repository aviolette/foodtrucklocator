package foodtruck.server.vendor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.server.resources.json.LocationWriter;

/**
 * @author aviolette
 * @since 12/8/16
 */
@Singleton
public class VendorLocationEditServlet extends VendorServletSupport {
  private static final Logger log = Logger.getLogger(VendorLocationEditServlet.class.getName());

  private static final String JSP = "/WEB-INF/jsp/vendor/locationEdit.jsp";
  private final StaticConfig config;
  private final LocationWriter locationWriter;
  private final LocationDAO locationDAO;

  @Inject
  public VendorLocationEditServlet(TruckDAO dao, UserService userService, Provider<SessionUser> sessionUserProvider,
      LocationDAO locationDAO, StaticConfig config, LocationWriter locationWriter) {
    super(dao, userService, sessionUserProvider);
    this.locationDAO = locationDAO;
    this.config = config;
    this.locationWriter = locationWriter;
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Truck truck) throws ServletException, IOException {
    String locationId = req.getRequestURI().substring(18);
    Location location = locationDAO.findById(Long.parseLong(locationId.substring(0, locationId.lastIndexOf('/'))));
    if (location == null) {
      resp.sendError(404);
      return;
    }
    log.info("Location: " + location);

    req = new GuiceHackRequestWrapper(req, JSP);
    req.setAttribute("googleApiKey", config.getGoogleJavascriptApiKey());
    try {
      req.setAttribute("location", locationWriter.asJSON(location));
    } catch (JSONException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      resp.sendError(500);
      return;
    }
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }
}
