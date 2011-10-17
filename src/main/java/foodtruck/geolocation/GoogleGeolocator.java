package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Location;

/**
 * GeoLocator that uses google geocoding.
 * @author aviolette@gmail.com
 * @since 8/29/11
 */
public class GoogleGeolocator implements GeoLocator {
  private final Pattern latLongExpression;
  private final WebResource geolocationResource;
  private static final Logger log = Logger.getLogger(GoogleGeolocator.class.getName());

  @Inject
  public GoogleGeolocator(@GoogleEndPoint WebResource geolocationResource) {
    latLongExpression = Pattern.compile("([\\-|\\d|\\.]+),\\s*([\\-|\\d|\\.]+)[\\s*,\\s*]?");
    this.geolocationResource = geolocationResource;
  }

  @Override
  public Location locate(String location) {
    Location loc = parseLatLong(location);
    if (loc != null) {
      return loc;
    }
    return lookup(location);
  }

  private @Nullable Location lookup(String location) {
    JSONObject obj = geolocationResource.queryParam("address", location)
        .queryParam("sensor", "true")
        .get(JSONObject.class);
    try {
      log.log(Level.INFO, "Geolocation result for {0}: \n{1}",
          new Object[] {location, obj.toString()});
      JSONArray results = obj.getJSONArray("results");
      if (results.length() > 0) {
        JSONObject loc =
            results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
        return new Location(loc.getDouble("lat"), loc.getDouble("lng"), location);
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  @VisibleForTesting
  @Nullable Location parseLatLong(String location) {
    Matcher m = latLongExpression.matcher(location);
    if (m.find()) {
      String name = m.end() == location.length() ? null : location.substring(m.end()).trim();
      return new Location(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)),
          name);
    }
    return null;
  }
}
