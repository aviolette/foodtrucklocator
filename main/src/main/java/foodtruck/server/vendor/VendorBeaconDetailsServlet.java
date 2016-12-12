package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.http.HttpStatusCodes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.model.StaticConfig;
import foodtruck.model.TrackingDevice;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 10/27/16
 */
@Singleton
public class VendorBeaconDetailsServlet extends HttpServlet {
  public static final String JSP = "/WEB-INF/jsp/vendor/beaconDetails.jsp";
  private final TrackingDeviceDAO deviceDAO;
  private final StaticConfig config;

  @Inject
  public VendorBeaconDetailsServlet(TrackingDeviceDAO deviceDAO, StaticConfig config) {
    this.deviceDAO = deviceDAO;
    this.config = config;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request = new GuiceHackRequestWrapper(request, JSP);
    String uri = request.getRequestURI();
    String deviceId = uri.substring(uri.lastIndexOf('/') + 1);
    TrackingDevice device = deviceDAO.findById(Long.parseLong(deviceId));
    Truck truck = (Truck) request.getAttribute(VendorPageFilter.TRUCK);
    if (device == null || device.getTruckOwnerId() == null || truck == null) {
      response.sendError(404, "Device not found");
      return;
    } else if (!device.getTruckOwnerId()
        .equals(truck.getId())) {
      response.sendError(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED);
      return;
    }
    request.setAttribute("googleApiKey", config.getGoogleJavascriptApiKey());
    request.setAttribute("beacon", device);
    request.getRequestDispatcher(JSP)
        .forward(request, response);
  }
}
