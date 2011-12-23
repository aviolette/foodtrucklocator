package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Location;
import foodtruck.util.ServiceException;

/**
 * A Geolocator instance that uses Yahoo's PlaceFinder service.
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
  public Location locate(String location, GeolocationGranularity granularity) {
    try {
      JSONObject obj = yahooResource.findLocation(location);
      log.log(Level.INFO, "Geolocation result for {0}: \n{1}",
          new Object[] {location, obj.toString()});
      JSONObject resultSet = obj.getJSONObject("ResultSet");
      if (resultSet.getInt("Found") == 0) {
        return null;
      }
      if (resultSet.getInt("Quality") < 40) {
        log.log(Level.INFO, "Result Set was too broad");
        return null;
      }
      JSONArray results = resultSet.getJSONArray("Results");
      JSONObject result = results.getJSONObject(0);
      if (result.getInt("quality") < 40) {
        log.log(Level.INFO, "Result was too broad");
        return null;
      }
      return new Location(Double.parseDouble(result.getString("latitude")),
          Double.parseDouble(result.getString("longitude")), location);
    } catch (JSONException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    } catch (ServiceException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
    return null;
  }
}
