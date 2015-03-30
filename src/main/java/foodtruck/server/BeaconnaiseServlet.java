package foodtruck.server;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 7/12/13
 */
@Singleton
public class BeaconnaiseServlet extends VendorServletSupport {
  private static final String JSP = "/WEB-INF/jsp/vendor/beaconnaise.jsp";

  @Inject
  public BeaconnaiseServlet(TruckDAO truckDAO) {
    super(truckDAO);
  }

  @Override protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp, @Nullable Truck truck)
      throws ServletException, IOException {
    req.getRequestDispatcher(JSP).forward(req, resp);
  }
}
