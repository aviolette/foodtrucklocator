package foodtruck.server.delivery;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.StaticConfig;
import foodtruck.server.FrontPageServlet;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 12/24/13
 */
@Singleton
public class RequestATruckLandingServlet extends FrontPageServlet {
  private static final String LANDING_JSP = "/WEB-INF/jsp/requestATruckLanding.jsp";

  @Inject
  public RequestATruckLandingServlet(ConfigurationDAO configDAO, StaticConfig staticConfig) {
    super(configDAO, staticConfig);
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (!configurationDAO.find().isFoodTruckRequestOn()) {
      resp.setStatus(404);
      return;
    }
    final Principal userPrincipal = req.getUserPrincipal();
    if (userPrincipal == null) {
      req = new GuiceHackRequestWrapper(req, LANDING_JSP);
      req.getRequestDispatcher(LANDING_JSP).forward(req, resp);
    } else {
      resp.sendRedirect("/requests/edit/new");
    }
  }
}
