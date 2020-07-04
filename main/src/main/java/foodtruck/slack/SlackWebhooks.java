package foodtruck.slack;

import java.io.UnsupportedEncodingException;

import org.codehaus.jettison.json.JSONException;

import foodtruck.model.SlackWebhook;

public interface SlackWebhooks {

  SlackWebhook create(
      String code) throws SlackAuthenticationFailedException, UnsupportedEncodingException, JSONException;
}
