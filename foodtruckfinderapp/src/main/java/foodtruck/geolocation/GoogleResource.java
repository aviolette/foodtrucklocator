package foodtruck.geolocation;

import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONObject;

/**
 * @author aviolette@gmail.com
 * @since 10/18/11
 */
public class GoogleResource {
  private final WebResource geolocationResource;

  @Inject
  public GoogleResource(@GoogleEndPoint WebResource geolocationResource) {
    this.geolocationResource = geolocationResource;
  }

  public JSONObject findLocation(String location) {
    return geolocationResource.queryParam("address", location)
        .queryParam("sensor", "false")
        .get(JSONObject.class);
  }
}
