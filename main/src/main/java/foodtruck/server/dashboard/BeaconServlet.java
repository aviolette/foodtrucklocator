package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.annotations.GoogleJavascriptApiKey;
import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.TrackingDevice;
import foodtruck.server.CodedServletException;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 7/30/16
 */
@Singleton
public class BeaconServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/beacon.jsp";
  private final TrackingDeviceDAO trackingDeviceDAO;
  private final TruckDAO truckDAO;
  private final String googleApiKey;

  @Inject
  public BeaconServlet(TrackingDeviceDAO trackingDeviceDAO, TruckDAO truckDAO, @GoogleJavascriptApiKey String googleAPIKey) {
    this.trackingDeviceDAO = trackingDeviceDAO;
    this.truckDAO = truckDAO;
    this.googleApiKey = googleAPIKey;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP_PATH);
    String id = req.getRequestURI().substring(req.getRequestURI().lastIndexOf('/')+1);
    TrackingDevice device = trackingDeviceDAO.findByIdOpt(Long.parseLong(id))
        .orElseThrow(() -> new CodedServletException(404, "Beacon not found: " + id));
    req.setAttribute("nav", "beacons");
    req.setAttribute("title", "Beacons");
    req.setAttribute("beacon", device);
    req.setAttribute("googleApiKey", googleApiKey);
    req.setAttribute("trucks", truckDAO.findActiveTrucks());
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP_PATH);
    String id = req.getRequestURI().substring(req.getRequestURI().lastIndexOf('/')+1);
    TrackingDevice device = trackingDeviceDAO.findByIdOpt(Long.parseLong(id))
        .orElseThrow(() -> new CodedServletException(404, "Beacon not found: " + id));
    String associatedTruck = req.getParameter("associatedTruck");
    if ("unset".equals(associatedTruck)) {
      associatedTruck = null;
    }
    trackingDeviceDAO.save(TrackingDevice.builder(device)
        .truckOwnerId(associatedTruck)
        .enabled("on".equals(req.getParameter("enabled")))
        .build());
    resp.sendRedirect("/admin/beacons");
  }
}
