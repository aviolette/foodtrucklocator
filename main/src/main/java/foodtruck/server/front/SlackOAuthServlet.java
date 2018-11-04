package foodtruck.server.front;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import com.sun.jersey.api.client.Client;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.SlackWebhookDAO;
import foodtruck.model.SlackWebhook;
import foodtruck.session.Session;

/**
 * @author aviolette
 * @since 11/4/18
 */
@Singleton
public class SlackOAuthServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(SlackOAuthServlet.class.getName());

  private final Client client;
  private final SlackWebhookDAO webookDAO;
  private final Provider<Session> sessionProvider;

  @Inject
  public SlackOAuthServlet(Client client, SlackWebhookDAO slackWebhookDAO, Provider<Session> sessionProvider) {
    this.client = client;
    this.webookDAO = slackWebhookDAO;
    this.sessionProvider = sessionProvider;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String code = req.getParameter("code");
    if (Strings.isNullOrEmpty(code)) {
      resp.sendError(400, "No request code sent");
      return;
    }

    String slackCode = (String) sessionProvider.get().getProperty("slackCode");
    if (Strings.isNullOrEmpty(slackCode)) {
      resp.sendError(500, "No slack code in session");
      return;
    } else {
      String state = req.getParameter("state");
      if (!slackCode.equals(state)) {
        log.log(Level.INFO, "Slack state does not match {0} : {1}", new Object[] {slackCode, state});
        resp.sendError(401, "Slack code does not match state");
        return;
      }
    }

    // TODO: also verify that state matches what we passed in

    // TODO: these should be injected

    String clientId = System.getProperty("foodtrucklocator.slack.client_id");
    String clientSecret = System.getProperty("foodtrucklocator.slack.client_secret");

    if (Strings.isNullOrEmpty(clientId) || Strings.isNullOrEmpty(clientSecret)) {
      resp.sendError(500, "Server not configured for this request");
      return;
    }

    String requestString = "client_id=" + clientId + "&client_secret=" + clientSecret + "&code=" + code;

    JSONObject response = client.resource("https://slack.com/api/oauth.access")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
        .entity(requestString)
        .post(JSONObject.class);

    try {
      if (!response.getBoolean("ok")) {
        log.log(Level.INFO, "Failed authentication: {0}", response);
        resp.sendError(401);
        return;
      }

      String teamId = response.getString("team_id");
      SlackWebhook webhook = webookDAO.findByTeamId(teamId).orElse(SlackWebhook.builder()
          .teamId(teamId)
          .locationName("Wacker and Adams, Chicago, IL")
          .build());

      webhook = SlackWebhook.builder(webhook)
          .webhookUrl(response.getJSONObject("incoming_webhook").getString("url"))
          .accessToken(response.getString("access_token"))
          .build();

      webookDAO.save(webhook);

    } catch (JSONException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      resp.sendError(500);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("world");
  }
}
