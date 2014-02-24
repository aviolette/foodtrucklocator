package foodtruck.server.delivery;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.FoodTruckRequestDAO;
import foodtruck.model.FoodTruckRequest;
import foodtruck.server.FrontPageServlet;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 2/23/14
 */
@Singleton
public class ViewRequestATruckServlet extends FrontPageServlet {
  private final FoodTruckRequestDAO foodTruckRequestDAO;

  @Inject
  public ViewRequestATruckServlet(ConfigurationDAO configDAO, FoodTruckRequestDAO requestDAO) {
    super(configDAO);
    this.foodTruckRequestDAO = requestDAO;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    int index = requestURI.indexOf("/requests/view/");
    String id = index == 0 ? requestURI.substring(15) : null;
    FoodTruckRequest foodTruckRequest = foodTruckRequestDAO.findById(Long.parseLong(id));
    if (foodTruckRequest == null) {
      resp.setStatus(404);
    } else {
      String jsp = "/WEB-INF/jsp/requestATruckView.jsp";
      req = new GuiceHackRequestWrapper(req, jsp);
      req.setAttribute("foodTruckRequest", foodTruckRequest);
      req.setAttribute("title", "Event - " + foodTruckRequest.getEventName());
      req.getRequestDispatcher(jsp).forward(req, resp);
    }
  }
}
