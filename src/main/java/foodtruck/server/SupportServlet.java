package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.model.StaticConfig;

/**
 * @author aviolette
 * @since 6/17/15
 */
@Singleton
public class SupportServlet extends FrontPageServlet {

  @Inject
  public SupportServlet(StaticConfig staticConfig) {
    super(staticConfig);
  }

  @Override
  protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setAttribute("title", "iPhone App Support");
    req.getRequestDispatcher("/WEB-INF/jsp/support/index.jsp").forward(req, resp);
  }
}
