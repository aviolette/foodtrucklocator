package foodtruck.server.vendor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.MenuDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Menu;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 8/17/16
 */
@Singleton
public class MenuServlet extends VendorServletSupport {
  private static final Logger log = Logger.getLogger(MenuServlet.class.getName());

  private static final String JSP = "/WEB-INF/jsp/vendor/menu.jsp";
  private final MenuDAO menuDAO;

  @Inject
  public MenuServlet(TruckDAO dao, UserService userService, MenuDAO menuDAO,
      Provider<SessionUser> sessionUserProvider) {
    super(dao, userService, sessionUserProvider);
    this.menuDAO = menuDAO;
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Truck truck) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
    req.setAttribute("tab", "menu");
    if (truck != null) {
      req.setAttribute("menu", menuDAO.findByTruck(truck.getId()));
    }
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }

  @Override
  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, String truckId) throws IOException {
    try {
      JSONObject jsonPayload = new JSONObject(new String(ByteStreams.toByteArray(req.getInputStream())));
      Menu menu = Menu.builder(menuDAO.findByTruck(truckId))
          .payload(jsonPayload.toString())
          .truckId(truckId)
          .build();
      menuDAO.save(menu);
    } catch (JSONException je) {
      log.log(Level.SEVERE, je.getMessage(), je);
      resp.sendError(400);
    }
  }
}
