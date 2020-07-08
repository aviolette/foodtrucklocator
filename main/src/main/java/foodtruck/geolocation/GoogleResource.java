package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONObject;

import foodtruck.annotations.GoogleServerAPIKey;
import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since 10/18/11
 */
class GoogleResource {
  private static final Logger log = Logger.getLogger(GoogleResource.class.getName());

  private final WebResource geolocationResource;
  private final String apiKey;
  private final String state;

  @Inject
  public GoogleResource(@GoogleEndPoint WebResource geolocationResource, @GoogleServerAPIKey String apiKey,
      @Named("foodtrucklocator.state") String state) {
    this.geolocationResource = geolocationResource;
    this.apiKey = apiKey;
    this.state = state;
  }

  JSONObject findLocation(String location) {
    // TODO: make country and state configurable
    WebResource resource = geolocationResource.queryParam("address", location)
        .queryParam("sensor", "false")
        .queryParam("components", "country:US|administrative_area:" + state);
    resource = resource.queryParam("key", apiKey);
    log.log(Level.INFO, "Requesting web resource {0}", resource);
    return resource.get(JSONObject.class);
  }

  JSONObject reverseLookup(Location location) {
    WebResource resource = geolocationResource.queryParam("latlng", location.getLatitude() + "," +
        location.getLongitude())
        .queryParam("sensor", "false");
    resource = resource.queryParam("key", apiKey);
    return resource.get(JSONObject.class);
  }
}
