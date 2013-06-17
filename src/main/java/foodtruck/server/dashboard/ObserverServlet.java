package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckObserverDAO;

/**
 * @author aviolette
 * @since 6/10/13
 */
@Singleton
public class ObserverServlet extends HttpServlet {
  private final TruckObserverDAO truckObserverDAO;

  @Inject
  public ObserverServlet(TruckObserverDAO truckObserverDAO) {
    this.truckObserverDAO = truckObserverDAO;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("lookouts", truckObserverDAO.findAll());
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/lookouts.jsp").forward(req, resp);
  }
}
