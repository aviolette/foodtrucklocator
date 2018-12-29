package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.datalake.DataExportService;

/**
 * @author aviolette
 * @since 2018-12-29
 */
@Singleton
public class MonthlyTruckStatsServlet extends AbstractJobServlet {

  private static final Logger log = Logger.getLogger(MonthlyTruckStatsServlet.class.getName());

  private final DataExportService service;

  @Inject
  public MonthlyTruckStatsServlet(DataExportService service) {
    this.service = service;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    int month = Integer.parseInt(req.getParameter("month"));
    int year = Integer.parseInt(req.getParameter("year"));
    try {
      service.exportStopsForMonth(year, month);
    } catch (Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}
