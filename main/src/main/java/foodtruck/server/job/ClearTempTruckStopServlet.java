package foodtruck.server.job;

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
public class ClearTempTruckStopServlet extends HttpServlet {

  private final TempTruckStopDAO dao;

  @Inject
  public ClearTempTruckStopServlet(TempTruckStopDAO dao) {
    this.dao = dao;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    dao.deleteAll();
  }
}
