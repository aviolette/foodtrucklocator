package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.StaticConfig;
import foodtruck.stats.HeatmapService;

/**
 * @author aviolette
 * @since 4/30/14
 */
@Singleton
public class HeatmapServlet extends FrontPageServlet {
  private static final String JSP = "/WEB-INF/jsp/heatmap.jsp";
  private final HeatmapService heatmapService;

  @Inject
  public HeatmapServlet(HeatmapService heatmapService, ConfigurationDAO configDAO, StaticConfig staticConfig) {
    super(configDAO, staticConfig);
    this.heatmapService = heatmapService;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("locations", heatmapService.get());
    req.setAttribute("center", configurationDAO.find().getCenter());
    req.setAttribute("title", "Heatmap of Chicago Food Truck Stops");
    req.setAttribute("description", "Heatmap showing popular food truck stops in Chicago-land.");
    req.getRequestDispatcher(JSP).forward(req, resp);
  }
}
