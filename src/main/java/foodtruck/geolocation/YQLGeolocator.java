package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Location;
import foodtruck.util.ServiceException;

/**
 * @author aviolette
 * @since 4/29/13
 */
public class YQLGeolocator implements GeoLocator {
  private static final Logger log = Logger.getLogger(YQLGeolocator.class.getName());
  private final YQLResource yahooResource;

  @Inject
  public YQLGeolocator(YQLResource yahooResource) {
    this.yahooResource = yahooResource;
  }

  @Nullable @Override public Location locate(String location, GeolocationGranularity granularity)
      throws ServiceException {
    try {
      JSONObject obj = yahooResource.findLocation(location, false);
      return parseResponse(location, obj);
    } catch (JSONException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    } catch (ServiceException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
    return null;
  }

  private @Nullable Location parseResponse(String location, JSONObject response) throws JSONException {
    log.log(Level.INFO, "Geolocation result for {0}: \n{1}",
        new Object[] {location, response.toString()});
    JSONObject resultSet = response.getJSONObject("query");
    int count = resultSet.getInt("count");
    if (count == 0) {
      return null;
    }
    JSONObject result;
    if (count == 1) {
      result = resultSet.getJSONObject("results");
    } else {
      JSONArray results = resultSet.getJSONArray("results");
      result = results.getJSONObject(0);
    }
    result = result.getJSONObject("Result");
    if (result.getInt("quality") < 40) {
      log.log(Level.INFO, "Result was too broad");
      return null;
    }
    String name = Strings.isNullOrEmpty(location) ? result.getString("line1") : location;
    return Location.builder().lat(Double.parseDouble(result.getString("latitude")))
        .lng(Double.parseDouble(result.getString("longitude"))).name(name).build();
  }

  @Nullable @Override public Location reverseLookup(Location location) throws ServiceException {
    return null;
  }
}
