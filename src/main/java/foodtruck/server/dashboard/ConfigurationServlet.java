package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;

/**
 * @author aviolette@gmail.com
 * @since 4/10/12
 */
@Singleton
public class ConfigurationServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/configuration.jsp";
  private ConfigurationDAO configDAO;

  @Inject
  public ConfigurationServlet(ConfigurationDAO configDAO) {
    this.configDAO = configDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Configuration config = configDAO.findSingleton();
    req.setAttribute("config", config);
    req.setAttribute("nav", "settings");
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Configuration config = configDAO.findSingleton();
    config = Configuration.builder(config)
        .yahooGeolocationEnabled("on".equals(req.getParameter("yahooGeolocationEnabled")))
        .googleGeolocationEnabled("on".equals(req.getParameter("googleGeolocationEnabled")))
        .tweetUpdateServletEnabled("on".equals(req.getParameter("tweetUpdateServletEnabled")))
        .build();
    configDAO.save(config);
    resp.sendRedirect("/admin/configuration");
  }
}
