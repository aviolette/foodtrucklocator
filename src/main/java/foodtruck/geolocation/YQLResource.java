package foodtruck.geolocation;

import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.util.ServiceException;

/**
 * @author aviolette
 * @since 4/29/13
 */
public class YQLResource {
  private final WebResource resource;
  private final ConfigurationDAO configDAO;

  @Inject
  public YQLResource(@YQLEndPoint WebResource resource, ConfigurationDAO configDAO) {
    this.configDAO = configDAO;
    this.resource = resource;
  }

  public JSONObject findLocation(String location, boolean reverse) {
    try {
      location = "select * from geo.placefinder where text = \"" + location + "\"";
      // TODO: Make locale a configurable parameter
      WebResource r = resource.queryParam("q", location)
          .queryParam("format", "json")
              // limit results to the specified locale
          .queryParam("gflags", "L")
          .queryParam("locale", "en_US")
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
