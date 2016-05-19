package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since 10/18/11
 */
public class GoogleResource {
  private static final Logger log = Logger.getLogger(GoogleResource.class.getName());

  private final WebResource geolocationResource;
  private final Optional<String> apiKey;

  @Inject
  public GoogleResource(@GoogleEndPoint WebResource geolocationResource, @GoogleServerApiKey Optional<String> apiKey) {
    this.geolocationResource = geolocationResource;
    this.apiKey = apiKey;
  }

  public JSONObject findLocation(String location) {
    // TODO: make country and state configurable
    WebResource resource = geolocationResource.queryParam("address", location)
        .queryParam("sensor", "false")
        .queryParam("components", "country:US|administrative_area:IL");
    if (apiKey.isPresent()) {
      String key = apiKey.get();
      resource = resource.queryParam("key",key);
    }
    log.log(Level.INFO, "Requesting web resource {0}", resource);
    return resource.get(JSONObject.class);
  }

  public JSONObject reverseLookup(Location location) {
    WebResource resource = geolocationResource.queryParam("latlng", location.getLatitude() + "," +
        location.getLongitude())
        .queryParam("sensor", "false");
    if (apiKey.isPresent()) {
      resource = resource.queryParam("key", apiKey.get());
    }
    return resource.get(JSONObject.class);
  }
}
