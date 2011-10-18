package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since 10/16/11
 */
public class YahooGeolocator implements GeoLocator {
  private static final Logger log = Logger.getLogger(YahooGeolocator.class.getName());
  private final YahooResource yahooResource;

  @Inject
  public YahooGeolocator(YahooResource yahooResource) {
    this.yahooResource = yahooResource;
  }

  @Override
  public Location locate(String location) {
    JSONObject obj = yahooResource.findLocation(location);
    try {
      log.log(Level.INFO, "Geolocation result for {0}: \n{1}",
          new Object[] {location, obj.toString()});
      JSONObject resultSet = obj.getJSONObject("ResultSet");
      if (resultSet.getInt("Found") == 0) {
        return null;
      }
      JSONArray results = resultSet.getJSONArray("Results");
      JSONObject result = results.getJSONObject(0);
      if (result.getInt("quality") < 40) {
        log.log(Level.INFO, "Result was too granular");
        return null;
      }
      return new Location(Double.parseDouble(result.getString("latitude")),
          Double.parseDouble(result.getString("longitude")), location);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }
}
