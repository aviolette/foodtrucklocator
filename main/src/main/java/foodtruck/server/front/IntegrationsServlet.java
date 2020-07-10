package foodtruck.server.front;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Random;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.annotations.BaseUrl;
import foodtruck.session.Session;

import static foodtruck.slack.SlackWebhooksImpl.CLIENT_ID;

/**
 * @author aviolette
 * @since 11/3/18
 */
@Singleton
public class IntegrationsServlet extends HttpServlet {

  private final Provider<Session> sessionProvider;
  private final String slackClientId;
  private final String baseUrl;

  @Inject
  public IntegrationsServlet(Provider<Session> sessionProvider, @Named(CLIENT_ID) String clientId,
      @BaseUrl String baseUrl) {
    this.sessionProvider = sessionProvider;
    this.slackClientId = clientId;
    this.baseUrl = baseUrl;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Session session = sessionProvider.get();
    String code = String.valueOf((new Random()).nextInt());
    session.setProperty("slackCode", code);
    req.setAttribute("encodedSlackUrl", URLEncoder.encode(baseUrl + "/slack/oauth", "UTF-8"));
    req.setAttribute("slackCode", code);
    req.setAttribute("slackClientId", slackClientId);
    req.setAttribute("tab", "integrations");
    req.getRequestDispatcher("/WEB-INF/jsp/integrations.jsp")
        .forward(req, resp);
  }
}
