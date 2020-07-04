package foodtruck.server.front;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.model.SlackWebhook;
import foodtruck.session.Session;
import foodtruck.slack.SlackAuthenticationFailedException;
import foodtruck.slack.SlackWebhooks;

/**
 * @author aviolette
 * @since 11/4/18
 */
@Singleton
public class SlackOAuthServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(SlackOAuthServlet.class.getName());

  private final Provider<Session> sessionProvider;
  private final SlackWebhooks webhooks;

  @Inject
  public SlackOAuthServlet(Provider<Session> sessionProvider, SlackWebhooks webhooks) {
    this.sessionProvider = sessionProvider;
    this.webhooks = webhooks;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String code = req.getParameter("code");
    if (Strings.isNullOrEmpty(code)) {
      resp.sendError(400, "No request code sent");
      return;
    }

    Session session = sessionProvider.get();
    String slackCode = (String) session.getProperty("slackCode");
    if (Strings.isNullOrEmpty(slackCode)) {
      resp.sendError(500, "No slack code in session");
      return;
    } else {
      String state = req.getParameter("state");
      if (!slackCode.equals(state)) {
        log.log(Level.INFO, "Slack state does not match {0} : {1}", new Object[]{slackCode, state});
        resp.sendError(401, "Slack code does not match state");
        return;
      }
    }

    try {
      SlackWebhook webhook = webhooks.create(code);
      session.setProperty("slackTeam", webhook.getTeamId());
      resp.sendRedirect("/slack/select_location");
    } catch (SlackAuthenticationFailedException e) {
      resp.sendError(401);
    } catch (Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      resp.sendError(500);
    }
  }
}
