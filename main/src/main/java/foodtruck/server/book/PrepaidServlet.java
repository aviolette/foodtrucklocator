package foodtruck.server.book;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.session.Session;

/**
 * @author aviolette
 * @since 11/13/16
 */
@Singleton
public class PrepaidServlet extends AuthenticatedServlet {
  public static final String JSP = "/WEB-INF/jsp/book/prepaid.jsp";

  @Inject
  public PrepaidServlet(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  protected void protectedDoGet(HttpServletRequest request, HttpServletResponse response, Principal principal) throws ServletException, IOException {
    request.getRequestDispatcher(JSP)
        .forward(request, response);
  }
}
