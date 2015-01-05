package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.StaticConfig;

/**
 * @author aviolette
 * @since 8/19/14
 */
@Singleton
public class AboutServlet extends FrontPageServlet {

  @Inject
  public AboutServlet(ConfigurationDAO configDAO, StaticConfig staticConfig) {
    super(configDAO, staticConfig);
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("tab", "about");
    req.getRequestDispatcher("/WEB-INF/jsp/about.jsp").forward(req, resp);
  }
}
