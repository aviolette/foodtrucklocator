package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author aviolette
 * @since 10/27/15
 */
public class SpecialsServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setAttribute("nav", "specials");
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/specials.jsp").forward(req, resp);
  }
}
