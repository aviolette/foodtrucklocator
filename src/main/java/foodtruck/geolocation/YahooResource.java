package foodtruck.geolocation;

import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.util.ServiceException;

/**
 * Extracting out the resource for easier testing
 * @author aviolette@gmail.com
 * @since 10/18/11
 */
public class YahooResource {
  private final WebResource resource;
  private final ConfigurationDAO configDAO;

  @Inject
  public YahooResource(@YahooEndPoint WebResource resource, ConfigurationDAO configDAO) {
    this.configDAO = configDAO;
    this.resource = resource;
  }

  /**
   * Calls the geolocation service and returns the JSON payload
   * @param location the location
   * @return the JSON Object
   * @throws ServiceException if an error occurs calling the service
   */
  public JSONObject findLocation(String location, boolean reverse) throws ServiceException {
    try {
      WebResource r = resource.queryParam("q", location)
          .queryParam("flags", "j")
          .queryParam("appid", configDAO.find().getYahooAppId());
      if (reverse) {
        r = r.queryParam("gflags", "R");
      }
      return r.header("Accept", "text/json; application/json")
          .get(JSONObject.class);
    } catch (RuntimeException rte) {
      throw new ServiceException(rte);
    }
  }
}
