package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 7/28/16
 */
@Singleton
public class BeaconsServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/beacons.jsp";
  private final TrackingDeviceDAO trackingDeviceDAO;

  @Inject
  public BeaconsServlet(TrackingDeviceDAO trackingDeviceDAO) {
    this.trackingDeviceDAO = trackingDeviceDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP_PATH);
    req.setAttribute("nav", "beacons");
    req.setAttribute("devices",  trackingDeviceDAO.findAll());
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }
}
