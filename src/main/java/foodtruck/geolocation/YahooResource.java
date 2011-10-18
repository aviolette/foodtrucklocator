package foodtruck.geolocation;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONObject;

/**
 * Extracting out the resource for easier testing
 * @author aviolette@gmail.com
 * @since 10/18/11
 */
public class YahooResource {
  private final WebResource resource;
  private String yahooId;

  @Inject
  public YahooResource(@YahooEndPoint WebResource resource, @Named("yahoo.app.id") String yahooId) {
    this.yahooId = yahooId;
    this.resource = resource;
  }

  public JSONObject findLocation(String location) {
    return resource.queryParam("q", location)
        .queryParam("flags", "j")
        .queryParam("appid", yahooId)
        .get(JSONObject.class);
  }
}
