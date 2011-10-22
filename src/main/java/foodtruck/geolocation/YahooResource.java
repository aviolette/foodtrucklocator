package foodtruck.geolocation;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONObject;

import foodtruck.util.ServiceException;

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

  /**
   * Calls the geolocation service and returns the JSON payload
   * @param location the location
   * @return the JSON Object
   * @throws ServiceException if an error occurs calling the service
   */
  public JSONObject findLocation(String location) throws ServiceException {
    try {
      return resource.queryParam("q", location)
          .queryParam("flags", "j")
          .queryParam("appid", yahooId)
          .header("Accept", "text/json; application/json")
          .get(JSONObject.class);
    } catch (RuntimeException rte) {
      throw new ServiceException(rte);
    }
  }
}
