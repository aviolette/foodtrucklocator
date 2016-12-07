package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.LocationDAO;

/**
 * @author aviolette
 * @since 7/7/16
 */
@Singleton
public class PopularServlet extends HttpServlet {
  private final LocationDAO locationDAO;

  @Inject
  public PopularServlet(LocationDAO locationDAO) {
    this.locationDAO = locationDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String jsp = "/WEB-INF/jsp/popular.jsp";
    req.setAttribute("locations", locationDAO.findPopularLocations());
    req.setAttribute("title", "Popular Spots");
    req.setAttribute("description", "Popular Food Truck Locations");
    req.setAttribute("tab", "location");
    req.getRequestDispatcher(jsp)
        .forward(req, resp);
  }
}
