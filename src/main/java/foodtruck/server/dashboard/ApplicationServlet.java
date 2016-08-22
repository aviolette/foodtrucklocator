package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 1/25/13
 */
@Singleton
public class ApplicationServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("nav", "applications");
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/applications.jsp").forward(req, resp);
  }
}
