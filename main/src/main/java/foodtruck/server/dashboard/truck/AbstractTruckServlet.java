package foodtruck.server.dashboard.truck;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableList;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.Link;

/**
 * @author aviolette
 * @since 11/25/16
 */
public abstract class AbstractTruckServlet extends HttpServlet {
  protected final TruckDAO truckDAO;

  protected AbstractTruckServlet(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @Override
  protected final void doGet(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    Truck truck = truckFromRequest(request);
    if (truck == null) {
      response.sendError(404);
      return;
    }
    request.setAttribute("nav", "trucks");
    request.setAttribute("truck", truck);
    request.setAttribute("breadcrumbs", breadcrumbs(truck));
    request = new GuiceHackRequestWrapper(request, getJsp());
    doGetProtected(request, response, truck);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Truck truck = truckFromRequest(request);
    if (truck == null) {
      response.sendError(404);
      return;
    }
    doPostProtected(request, response, truck);
  }

  protected void doPostProtected(HttpServletRequest request, HttpServletResponse response,
      Truck truck) throws IOException {
  }

  private Truck truckFromRequest(HttpServletRequest request) {
    final String requestURI = request.getRequestURI();
    String truckId = requestURI.substring(14);
    int idx = truckId.indexOf("/");
    if (idx != -1) {
      truckId = truckId.substring(0, idx);
    }
    return truckDAO.findById(truckId);
  }

  protected abstract ImmutableList<Link> breadcrumbs(Truck truck);

  protected void doGetProtected(HttpServletRequest request, HttpServletResponse response,
      Truck truck) throws ServletException, IOException {
    forward(request, response);
  }

  protected void forward(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    request.getRequestDispatcher(getJsp())
        .forward(request, response);
  }

  protected abstract String getJsp();
}
