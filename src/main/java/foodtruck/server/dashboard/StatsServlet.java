package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
@Singleton
public class StatsServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("nav", "stats");
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/stats.jsp").forward(req, resp);
  }
}
