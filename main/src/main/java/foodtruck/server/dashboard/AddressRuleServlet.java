package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;

/**
 * @author aviolette@gmail.com
 * @since 8/2/12
 */

@Singleton
public class AddressRuleServlet extends HttpServlet {
  private final TruckDAO truckDAO;

  @Inject
  public AddressRuleServlet(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("trucks", truckDAO.findActiveTrucks());
    req.setAttribute("nav", "addresses");
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/addresses.jsp").forward(req, resp);
  }
}
