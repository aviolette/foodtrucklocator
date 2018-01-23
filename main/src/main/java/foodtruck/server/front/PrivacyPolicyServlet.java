package foodtruck.server.front;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author aviolette
 * @since 1/23/18
 */
@Singleton
public class PrivacyPolicyServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("title", "Privacy Policy");
    req.setAttribute("description", "Privacy Policy");
    req.setAttribute("tab", "about");

    req.getRequestDispatcher("/WEB-INF/jsp/privacy.jsp").forward(req, resp);
  }
}
