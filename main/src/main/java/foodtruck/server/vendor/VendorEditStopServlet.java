package foodtruck.server.vendor;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import foodtruck.model.Truck;
import foodtruck.server.dashboard.EditStopHelper;

/**
 * @author aviolette
 * @since 1/21/17
 */
@Singleton
public class VendorEditStopServlet extends HttpServlet {

  private final EditStopHelper editStopHelper;

  @Inject
  public VendorEditStopServlet(EditStopHelper editStopHelper) {
    this.editStopHelper = editStopHelper;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String uri = req.getRequestURI();
    String stopId = uri.substring(uri.lastIndexOf('/') + 1, uri.length());
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    editStopHelper.setupEditPage(stopId, truck, req, resp, true);
  }
}
