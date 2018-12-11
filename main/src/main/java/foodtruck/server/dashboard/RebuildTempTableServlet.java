package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TempTruckStopDAO;

/**
 * @author aviolette
 * @since 2018-12-11
 */

@Singleton
public class RebuildTempTableServlet extends HttpServlet {

  private final TempTruckStopDAO dao;

  @Inject
  public RebuildTempTableServlet(TempTruckStopDAO dao) {
    this.dao = dao;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/rebuild_temp.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    dao.deleteAll();
    resp.sendRedirect("/cron/populate_imperial_oaks_stops?redirect=/admin/rebuild_temp");
  }
}
