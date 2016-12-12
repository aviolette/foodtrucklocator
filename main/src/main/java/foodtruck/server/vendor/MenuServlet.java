package foodtruck.server.vendor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.MenuDAO;
import foodtruck.model.Menu;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 8/17/16
 */
@Singleton
public class MenuServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(MenuServlet.class.getName());

  private static final String JSP = "/WEB-INF/jsp/vendor/menu.jsp";
  private final MenuDAO menuDAO;

  @Inject
  public MenuServlet(MenuDAO menuDAO) {
    this.menuDAO = menuDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
    req.setAttribute("tab", "menu");
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    req.setAttribute("menu", menuDAO.findByTruck(truck.getId()));
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      JSONObject jsonPayload = new JSONObject(new String(ByteStreams.toByteArray(req.getInputStream())));
      Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
      Menu menu = Menu.builder(menuDAO.findByTruck(truck.getId()))
          .payload(jsonPayload.toString())
          .truckId(truck.getId())
          .build();
      menuDAO.save(menu);
    } catch (JSONException je) {
      log.log(Level.SEVERE, je.getMessage(), je);
      resp.sendError(400);
    }
  }
}
