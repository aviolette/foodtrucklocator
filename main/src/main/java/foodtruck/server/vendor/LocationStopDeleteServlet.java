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

import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 7/18/16
 */
@Singleton
public class LocationStopDeleteServlet extends VendorServletSupport {
  private static final String JSP = "/WEB-INF/jsp/vendor/locationDelete.jsp";
  private final TruckStopDAO truckStopDAO;

  @Inject
  public LocationStopDeleteServlet(TruckDAO dao, UserService userService, TruckStopDAO truckStopDAO,
      Provider<SessionUser> sessionUserProvider) {
    super(dao, userService, sessionUserProvider);
    this.truckStopDAO = truckStopDAO;
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Location location) throws ServletException, IOException {
    TruckStop stop = extractTruckStop(req);
    if (stop == null) {
      resp.sendError(400, "Stop not found");
      return;
    }
    req.setAttribute("stopId", stop.getKey());
    req = new GuiceHackRequestWrapper(req, JSP);
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }

  @Override
  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, Location location,
      String principalName) throws IOException {
    TruckStop stop = extractTruckStop(req);
    if (stop == null) {
      resp.sendError(400, "Stop not found");
      return;
    }
    truckStopDAO.delete((Long) stop.getKey());
    resp.sendRedirect("/vendor/locations/" + location.getKey());
  }

  @Nullable
  private TruckStop extractTruckStop(HttpServletRequest req) {
    String uri = req.getRequestURI();
    String stopId = uri.substring(uri.substring(0, uri.length() - 7)
        .lastIndexOf('/') + 1, uri.length() - 7);
    return truckStopDAO.findById(Long.parseLong(stopId));
  }
}
