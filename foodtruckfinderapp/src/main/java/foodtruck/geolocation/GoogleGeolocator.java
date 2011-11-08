package foodtruck.geolocation;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

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
  private static final Logger log = Logger.getLogger(GoogleGeolocator.class.getName());
  private final GoogleResource googleResource;

  @Inject
  public GoogleGeolocator(GoogleResource googleResource) {
    latLongExpression = Pattern.compile("([\\-|\\d|\\.]+),\\s*([\\-|\\d|\\.]+)[\\s*,\\s*]?");
    this.googleResource = googleResource;
  }

  @Override
  public Location locate(String location) {
    Location loc = parseLatLong(location);
    if (loc != null) {
      return loc;
    }
    if (!isEnabled()) {
      return null;
    }
    return lookup(location);
  }

  private boolean isEnabled() {
    return "true".equals(System.getProperty("google.geolocator.enabled", "true"));
  }

  private @Nullable Location lookup(String location) {
    JSONObject obj = googleResource.findLocation(location);
    try {
      log.log(Level.INFO, "Geolocation result for {0}: \n{1}",
          new Object[] {location, obj.toString()});
      JSONArray results = obj.getJSONArray("results");
      if (results.length() > 0) {
        final JSONObject firstResult = results.getJSONObject(0);
        final JSONArray types = firstResult.getJSONArray("types");
        if (arrayContains(types, "locality")) {
          log.log(Level.INFO, "Result was too granular");
          return null;
        }
        JSONObject loc =
            firstResult.getJSONObject("geometry").getJSONObject("location");
        return new Location(loc.getDouble("lat"), loc.getDouble("lng"), location);
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  private boolean arrayContains(@Nullable JSONArray arr, String searchWord) throws JSONException {
    if (arr == null) {
      return false;
    }
    for (int i = 0; i < arr.length(); i++) {
      if (searchWord.equals(arr.getString(i))) {
        return true;
      }
    }
    return false;
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
