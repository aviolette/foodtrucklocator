package foodtruck.slack;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.annotations.BaseUrl;
import foodtruck.dao.SlackWebhookDAO;
import foodtruck.model.SlackWebhook;

public class SlackWebhooksImpl implements SlackWebhooks {

  public static final String CLIENT_ID = "slack.client_id";
  static final String CLIENT_SECRET = "slack.client_secret";

  private static final Logger log = Logger.getLogger(SlackWebhooks.class.getName());
  private final SlackWebhookDAO webhookDAO;
  private final Client client;
  private final String clientSecret;
  private final String clientId;
  private final String baseUrl;

  @Inject
  public SlackWebhooksImpl(Client client, SlackWebhookDAO webhookDAO, @Named(CLIENT_ID) String clientId, @Named(CLIENT_SECRET) String clientSecret, @BaseUrl String baseUrl) {
    this.webhookDAO = webhookDAO;
    this.client = client;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.baseUrl = baseUrl;
  }

  public SlackWebhook create(
      String code) throws SlackAuthenticationFailedException, UnsupportedEncodingException, JSONException {

    if (Strings.isNullOrEmpty(clientId) || Strings.isNullOrEmpty(clientSecret)) {
      throw new RuntimeException("Server not configured for this request");
    }
    String requestString =
        "client_id=" + clientId + "&client_secret=" + clientSecret + "&code=" + code + "&redirect_uri=" +
            URLEncoder.encode(baseUrl + "/slack/oauth", "UTF-8");
    JSONObject response = client.resource("https://slack.com/api/oauth.access")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
        .entity(requestString)
        .post(JSONObject.class);

    if (!response.getBoolean("ok")) {
      log.log(Level.INFO, "Failed authentication: {0}", response);
      throw new SlackAuthenticationFailedException();
    }
    String teamId = response.getString("team_id");
    SlackWebhook webHook = SlackWebhook.builder(webhookDAO.findByTeamId(teamId)
        .orElse(SlackWebhook.builder()
            .teamId(teamId)
            .locationName("Wacker and Adams, Chicago, IL")
            .build()))
        .webhookUrl(response.getJSONObject("incoming_webhook")
            .getString("url"))
        .accessToken(response.getString("access_token"))
        .build();

    webhookDAO.save(webHook);
    return webHook;
  }
}
