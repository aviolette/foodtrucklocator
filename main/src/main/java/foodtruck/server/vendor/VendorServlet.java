package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 10/15/13
 */
@Singleton
public class VendorServlet extends HttpServlet {
  private static final String JSP = "/WEB-INF/jsp/vendor/vendordash.jsp";
  private final LocationDAO locationDAO;
  private final BeaconServletHelper helper;

  @Inject
  public VendorServlet(BeaconServletHelper helper, LocationDAO locationDAO) {
    this.locationDAO = locationDAO;
    this.helper = helper;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    if (truck != null) {
      helper.seedRequest(req, truck);
    }
    req.setAttribute("locations", locationDAO.findLocationNamesAsJson());
    req.setAttribute("tab", "vendorhome");
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }
}
