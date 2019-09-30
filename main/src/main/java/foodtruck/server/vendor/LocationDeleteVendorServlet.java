package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.http.HttpStatusCodes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;

@Singleton
public class LocationDeleteVendorServlet extends HttpServlet {

  private final TruckStopDAO dao;

  @Inject
  public LocationDeleteVendorServlet(TruckStopDAO dao) {
    this.dao = dao;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Location location = (Location) req.getAttribute(VendorPageFilter.LOCATION);
    String uri = req.getRequestURI();
    uri = req.getRequestURI()
        .substring(0, uri.length() - 7);
    String stopId = uri.substring(uri.lastIndexOf('/') + 1);
    TruckStop stop = dao.findByIdOpt(Long.valueOf(stopId))
        .get();
    if (stop.getLocation().sameName(location)) {
      req.setAttribute("stopId", stopId);
      req.getRequestDispatcher("/WEB-INF/jsp/vendor/locationDelete.jsp")
          .forward(req, resp);
    } else {
      resp.sendError(HttpStatusCodes.STATUS_CODE_FORBIDDEN);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Location location = (Location) req.getAttribute(VendorPageFilter.LOCATION);
    String uri = req.getRequestURI();
    uri = req.getRequestURI()
        .substring(0, uri.length() - 7);
    String stopId = uri.substring(uri.lastIndexOf('/') + 1);
    TruckStop stop = dao.findByIdOpt(Long.valueOf(stopId)).get();
    if (stop.getLocation().sameName(location)) {
      dao.delete(Long.valueOf(stopId));
      resp.sendRedirect("/vendor/managed-location");
    } else {
      resp.sendError(HttpStatusCodes.STATUS_CODE_FORBIDDEN);
    }
  }
}
