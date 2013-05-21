package foodtruck.server;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;


/**
 * @author aviolette
 * @since 5/20/13
 */
@Singleton
public class TrucksServlet extends HttpServlet {
  private final TruckDAO trucks;

  @Inject
  public TrucksServlet(TruckDAO trucks) {
    this.trucks = trucks;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String jsp = "/WEB-INF/jsp/trucks.jsp";
    req.setAttribute("tab", "trucks");
    final Collection<Truck> trucks = this.trucks.findVisibleTrucks();
    req.setAttribute("trucks", trucks);
    req.setAttribute("containerType", "fixed");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
