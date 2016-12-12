package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * Unlinks a social media accounts credentials from a specific truck.
 *
 * @author aviolette
 * @since 11/8/16
 */
@Singleton
public class VendorUnlinkAccountServlet extends HttpServlet {
  private final TruckDAO truckDAO;

  @Inject
  public VendorUnlinkAccountServlet(TruckDAO dao) {
    this.truckDAO = dao;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    // TODO: for now I always assume they're unlinking a twitter account
    truck = truck.append()
        .clearTwitterCredentials()
        .build();
    truckDAO.save(truck);
    resp.sendRedirect("/vendor/socialmedia/" + truck.getId());
  }
}
