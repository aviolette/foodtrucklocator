package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ApplicationDAO;
import foodtruck.model.Application;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.Urls;

/**
 * @author aviolette
 * @since 6/19/15
 */
@Singleton
public class ApplicationDetailServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/application.jsp";
  private final ApplicationDAO applicationDAO;

  @Inject
  public ApplicationDetailServlet(ApplicationDAO applicationDAO) {
    this.applicationDAO = applicationDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final String path = Urls.stripSessionId(req.getRequestURI());
    final String appIndex = path.substring(path.lastIndexOf("/") + 1);
    Application app = applicationDAO.findById(appIndex);
    if (app == null) {
      resp.setStatus(404);
      return;
    }
    req = new GuiceHackRequestWrapper(req, JSP_PATH);
    req.setAttribute("nav", "applications");
    req.setAttribute("application", app);
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }
}
