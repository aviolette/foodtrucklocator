package foodtruck.server.book;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Provider;

import foodtruck.util.Session;

/**
 * A servlet that stores a principal in a session and redirects to login page if that principal
 * is not present.
 * @author aviolette
 * @since 11/13/16
 */
public abstract class AuthenticatedServlet extends HttpServlet {
  private final Provider<Session> sessionProvider;

  AuthenticatedServlet(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Principal principal = getPrincipal();
    if (principal == null) {
      resp.sendRedirect("/login");
      return;
    }
    req.setAttribute("user", principal);
    protectedDoGet(req, resp, principal);
  }

  private Principal getPrincipal() {
    Session session = sessionProvider.get();
    return (Principal) session.getProperty("principal");
  }

  protected abstract void protectedDoGet(HttpServletRequest request, HttpServletResponse resp, Principal principal) throws ServletException, IOException;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Principal principal = getPrincipal();
    if (principal == null) {
      resp.sendError(403);
      return;
    }
    protectedDoPost(req, resp, principal);
  }

  protected void protectedDoPost(HttpServletRequest request, HttpServletResponse response, Principal principal) {
    // default implementation does nothing
  }
}
