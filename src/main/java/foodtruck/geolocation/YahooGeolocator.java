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
import foodtruck.monitoring.Monitored;
import foodtruck.util.ServiceException;

/**
 * A Geolocator instance that uses Yahoo's PlaceFinder service.
 * @author aviolette@gmail.com
 * @since 10/16/11
 */
// Use the YQL Geolocator instead
@Deprecated
public class YahooGeolocator implements GeoLocator {
  private static final Logger log = Logger.getLogger(YahooGeolocator.class.getName());
  private final YahooResource yahooResource;

  @Inject
  public YahooGeolocator(YahooResource yahooResource) {
    this.yahooResource = yahooResource;
  }

  @Override @Monitored
  public Location locate(String location, GeolocationGranularity granularity) {
    try {
      JSONObject obj = yahooResource.findLocation(location, false);
      return parseResponse(location, obj, granularity);
    } catch (JSONException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    } catch (ServiceException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
    return null;
  }

  private Location parseResponse(@Nullable String location, JSONObject obj, GeolocationGranularity granularity) throws JSONException {
    log.log(Level.INFO, "Geolocation result for {0}: \n{1}",
        new Object[] {location, obj.toString()});
    JSONObject resultSet = obj.getJSONObject("ResultSet");
    if (resultSet.getInt("Found") == 0) {
      return null;
    }
    int quality = (granularity == GeolocationGranularity.BROAD) ? 50 : 80;
    // YAHOO ADRESS QUALITY: http://developer.yahoo.com/geo/placefinder/guide/responses.html#address-quality
    if (resultSet.getInt("Quality") < quality) {
      log.log(Level.INFO, "Result Set was too broad");
      return null;
    }
    JSONArray results = resultSet.getJSONArray("Results");
    JSONObject result = results.getJSONObject(0);
    if (result.getInt("quality") < 40) {
      log.log(Level.INFO, "Result was too broad");
      return null;
    }
    String name = Strings.isNullOrEmpty(location) ? result.getString("line1") : location;
    return Location.builder().lat(Double.parseDouble(result.getString("latitude")))
        .lng(Double.parseDouble(result.getString("longitude"))).name(name).build();
  }

  @Override public @Nullable Location reverseLookup(Location location) throws ServiceException {
    try {
      JSONObject obj = yahooResource.findLocation(location.getLatitude() + "," +
          location.getLongitude(), true);
      return parseResponse(null, obj, GeolocationGranularity.BROAD);
    } catch (ServiceException e) {
      throw new ServiceException(e);
    } catch (JSONException e) {
      throw new ServiceException(e);
    }
  }
}
