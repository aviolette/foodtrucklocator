package foodtruck.server.front;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.model.StaticConfig;

/**
 * @author aviolette
 * @since 5/20/13
 */
@Singleton
public class TrucksServlet extends HttpServlet {

  private static final String JSP = "/WEB-INF/jsp/trucks.jsp";
  private final StaticConfig staticConfig;

  @Inject
  public TrucksServlet(StaticConfig staticConfig) {
    this.staticConfig = staticConfig;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (req.getRequestURI()
        .endsWith("/")) {
      resp.sendRedirect("/trucks");
      return;
    }
    String tag = req.getParameter("tag");
    if (!Strings.isNullOrEmpty(tag)) {
      req.setAttribute("filteredBy", tag);
    }
    req.setAttribute("foodTruckRequestOn", false);
    req.setAttribute("description", "Catalogue of all the food trucks in " + staticConfig.getCity());
    req.setAttribute("title", "Food Trucks in " + staticConfig.getCity());
    req.setAttribute("tab", "trucks");
    req.setAttribute("supportsBooking", staticConfig.getSupportsBooking());
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }
}
