package foodtruck.server.book;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.session.Session;

/**
 * @author aviolette
 * @since 11/17/16
 */
@Singleton
public class LogoutServlet extends HttpServlet {
  private final Provider<Session> sessionProvider;

  @Inject
  public LogoutServlet(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    sessionProvider.get()
        .invalidate();
    resp.sendRedirect("/");
  }
}
