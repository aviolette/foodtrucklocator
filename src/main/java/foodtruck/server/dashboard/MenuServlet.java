package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
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

  public static JSONObject scrub(JSONObject payload) throws JSONException {
    Escaper escaper = HtmlEscapers.htmlEscaper();
    JSONArray sections = payload.getJSONArray("sections");
    for (int i = 0; i < sections.length(); i++) {
      JSONObject section = sections.getJSONObject(i);
      section.put("section", escaper.escape(section.getString("section")));
      String description = Strings.nullToEmpty(section.optString("description"));
      section.put("description", escaper.escape(description));
      JSONArray items = section.getJSONArray("items");
      for (int j = 0; j < items.length(); j++) {
        JSONObject item = items.getJSONObject(j);
        item.put("name", escaper.escape(item.getString("name")));
        item.put("description", escaper.escape(Strings.nullToEmpty(item.optString("description"))));
      }
    }
    return payload;
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
      jsonPayload = scrub(jsonPayload);
      log.info(jsonPayload.toString(2));
      String truckId = truckId(req);
      Menu menu = Menu.builder(menuDAO.findByTruck(truckId)).payload(jsonPayload.toString()).truckId(truckId).build();
      menuDAO.save(menu);
    } catch (JSONException je) {
      log.log(Level.SEVERE, je.getMessage(), je);
      resp.sendError(400);
    }
  }
}
