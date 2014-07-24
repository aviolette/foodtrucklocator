package foodtruck.server;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.resources.json.TruckCollectionWriter;

/**
 * @author aviolette
 * @since 7/16/14
 */

@Singleton
public class TruckTimelineServlet extends FrontPageServlet {
  private static final String JSP = "/WEB-INF/jsp/timeline.jsp";
  private final TruckDAO truckDAO;
  private final TruckCollectionWriter trucksWriter;

  @Inject
  public TruckTimelineServlet(ConfigurationDAO configDAO, TruckDAO truckDAO, TruckCollectionWriter trucksWriter) {
    super(configDAO);
    this.truckDAO = truckDAO;
    this.trucksWriter = trucksWriter;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    List<Truck> trucks = truckDAO.findVisibleTrucks();
    req.setAttribute("trucks", trucksWriter.asJSON(trucks).toString());
    req.getRequestDispatcher(JSP).forward(req, resp);
  }
}
