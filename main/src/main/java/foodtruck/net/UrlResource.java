package foodtruck.net;

import javax.ws.rs.core.HttpHeaders;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONArray;

import foodtruck.annotations.UserAgent;

/**
 * @author aviolette
 * @since 2019-01-28
 */
public class UrlResource {

  private final Client client;
  private final String userAgent;

  @Inject
  public UrlResource(Client client, @UserAgent String userAgent) {
    this.client = client;
    this.userAgent = userAgent;
  }

  public String getAsString(String endpoint) {
    return build(endpoint).get(String.class);
  }

  public JSONArray getAsArray(String endpoint) {
    return build(endpoint).get(JSONArray.class);
  }

  private WebResource.Builder build(String endpoint) {
    return client.resource(endpoint)
        .header(HttpHeaders.USER_AGENT, userAgent);
  }
}
