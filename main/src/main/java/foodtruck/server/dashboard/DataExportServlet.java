package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.joda.time.DateTime;

import foodtruck.datalake.DataExportService;
import foodtruck.time.Clock;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

/**
 * @author aviolette
 * @since 2018-12-29
 */
@Singleton
public class DataExportServlet extends HttpServlet {

  private final DataExportService service;
  private final Clock clock;
  private final Provider<Queue> queueProvider;

  @Inject
  public DataExportServlet(DataExportService service, Clock clock, Provider<Queue> queueProvider) {
    this.service = service;
    this.clock = clock;
    this.queueProvider = queueProvider;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/dataexport.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    service.exportTrucks();

    DateTime dt = new DateTime(2011, 8, 1, 0, 0, clock.zone());
    do {
      Queue queue = queueProvider.get();
      queue.add(withUrl("/cron/monthly_stop_stats_generate")
          .param("month", String.valueOf(dt.getMonthOfYear()))
          .param("year", String.valueOf(dt.getYear())));
      dt = dt.plusMonths(1);
    } while(dt.getYear() < 2019);
    resp.sendRedirect("/admin/dataexport");
  }
}
