package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.schedule.TempScheduleService;

/**
 * @author aviolette
 * @since 2018-12-11
 */
@Singleton
public class RebuildTempScheduleServlet extends HttpServlet {

  private final TempScheduleService service;

  @Inject
  public RebuildTempScheduleServlet(TempScheduleService service) {
    this.service = service;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    service.rebuild();
  }
}
