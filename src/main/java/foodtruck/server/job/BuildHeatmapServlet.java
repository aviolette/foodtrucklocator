package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.stats.HeatmapService;

/**
 * @author aviolette
 * @since 5/18/14
 */
@Singleton
public class BuildHeatmapServlet extends HttpServlet {
  private final HeatmapService heatmapService;

  @Inject
  public BuildHeatmapServlet(HeatmapService heatmapService) {
    this.heatmapService = heatmapService;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    heatmapService.rebuild();
  }
}
