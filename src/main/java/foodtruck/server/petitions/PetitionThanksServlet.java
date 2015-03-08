package foodtruck.server.petitions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.model.StaticConfig;
import foodtruck.server.FrontPageServlet;

/**
 * @author aviolette
 * @since 7/30/14
 */
@Singleton
public class PetitionThanksServlet extends FrontPageServlet {
  private static final String JSP = "/WEB-INF/jsp/petitions/600wthanks.jsp";

  @Inject
  public PetitionThanksServlet(StaticConfig staticConfig) {
    super(staticConfig);
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher(JSP).forward(req, resp);
  }
}
