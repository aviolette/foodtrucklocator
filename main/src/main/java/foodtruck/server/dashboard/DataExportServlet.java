package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.datalake.DataExportService;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2018-12-29
 */
@Singleton
public class DataExportServlet extends HttpServlet {

  private final DataExportService service;
  private final Clock clock;

  @Inject
  public DataExportServlet(DataExportService service, Clock clock) {
    this.service = service;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/dataexport.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    service.exportTrucks("cftf_datalake");
    resp.sendRedirect("/admin/dataexport");
  }
}
