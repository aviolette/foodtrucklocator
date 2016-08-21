package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.MenuDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Menu;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.Link;

/**
 * @author aviolette
 * @since 8/21/16
 */
@Singleton
public class MenuServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(foodtruck.server.vendor.MenuServlet.class.getName());

  private static final String JSP = "/WEB-INF/jsp/dashboard/menu.jsp";
  private final MenuDAO menuDAO;
  private final TruckDAO truckDAO;

  @Inject
  public MenuServlet(MenuDAO menuDAO, TruckDAO truckDAO) {
    this.menuDAO = menuDAO;
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
    req.setAttribute("tab", "truck");
    String truckId = truckId(req);
    Truck truck = truckDAO.findById(truckId);
    req.setAttribute("endpoint", req.getRequestURI());
    req.setAttribute("menu", menuDAO.findByTruck(truckId));
    req.setAttribute("truck", truckDAO.findById(truckId));
    req.setAttribute("breadcrumbs",
        ImmutableList.of(new Link("Trucks", "/admin/trucks"), new Link(truck.getName(), "/admin/trucks/" + truckId),
            new Link("Edit", "/admin/trucks/" + truckId + "/configuration")));

    req.getRequestDispatcher(JSP).forward(req, resp);
  }

  private String truckId(HttpServletRequest req) {
    final String requestURI = req.getRequestURI();
    String truckId = requestURI.substring(14);
    truckId = truckId.substring(0, truckId.length() - 5);
    return truckId;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      JSONObject jsonPayload = new JSONObject(new String(ByteStreams.toByteArray(req.getInputStream())));
      String truckId = truckId(req);
      Menu menu = Menu.builder(menuDAO.findByTruck(truckId)).payload(jsonPayload.toString()).truckId(truckId).build();
      menuDAO.save(menu);
    } catch (JSONException je) {
      log.log(Level.SEVERE, je.getMessage(), je);
      resp.sendError(400);
    }
  }
}
