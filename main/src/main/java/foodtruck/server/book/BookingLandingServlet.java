package foodtruck.server.book;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.model.StaticConfig;
import foodtruck.server.FrontPageServlet;

/**
 * @author aviolette
 * @since 11/11/16
 */
@Singleton
public class BookingLandingServlet extends FrontPageServlet {
  public static final String JSP = "/WEB-INF/jsp/book/landing.jsp";

  @Inject
  public BookingLandingServlet(StaticConfig staticConfig, Provider<UserService> userServiceProvider) {
    super(staticConfig);
  }

  @Override
  protected void doGetProtected(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    request.setAttribute("suffix", "");
    request.getRequestDispatcher(JSP)
        .forward(request, response);
  }
}
