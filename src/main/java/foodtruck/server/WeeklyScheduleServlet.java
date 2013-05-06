package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 5/6/13
 */
@Singleton
public class WeeklyScheduleServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String jsp = "/WEB-INF/jsp/weekly.jsp";
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
