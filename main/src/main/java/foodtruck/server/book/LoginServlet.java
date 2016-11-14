package foodtruck.server.book;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.util.Session;

/**
 * @author aviolette
 * @since 11/14/16
 */
@Singleton
public class LoginServlet extends HttpServlet {

  private final Provider<Session> sessionProvider;

  @Inject
  public LoginServlet(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    sessionProvider.get().invalidate();
    request.getRequestDispatcher("/WEB-INF/jsp/book/signin.jsp")
        .forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doPost(req, resp);
  }
}
