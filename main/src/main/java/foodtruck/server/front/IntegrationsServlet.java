package foodtruck.server.front;

import java.io.IOException;
import java.util.Random;

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
 * @since 11/3/18
 */
@Singleton
public class IntegrationsServlet extends HttpServlet {

  private final Provider<Session> sessionProvider;

  @Inject
  public IntegrationsServlet(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Session session = sessionProvider.get();
    String code = String.valueOf((new Random()).nextInt());
    session.setProperty("slackCode", code);
    req.setAttribute("slackCode", code);
    req.setAttribute("tab", "integrations");
    req.getRequestDispatcher("/WEB-INF/jsp/integrations.jsp")
        .forward(req, resp);
  }
}
