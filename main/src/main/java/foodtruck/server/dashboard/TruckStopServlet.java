package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 11/26/12
 */
@Singleton
public class TruckStopServlet extends HttpServlet {

  private final TruckDAO truckDAO;
  private final EditStopHelper editStopHelper;

  @Inject
  public TruckStopServlet(EditStopHelper editStopHelper, TruckDAO truckDAO) {
    this.editStopHelper = editStopHelper;
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    String truckId = requestURI.substring(14);
    if (Strings.isNullOrEmpty(truckId)) {
      resp.sendRedirect("/trucks");
      return;
    }
    int index = truckId.indexOf("/stops/");
    String actualTruckId = truckId.substring(0, index);
    String stopId = truckId.substring(index + 7);
    final Truck truck = truckDAO.findById(actualTruckId);
    editStopHelper.setupEditPage(stopId, truck, req, resp, false);
  }
}
