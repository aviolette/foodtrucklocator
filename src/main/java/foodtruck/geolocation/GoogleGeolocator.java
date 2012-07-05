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
  public Location locate(String location, GeolocationGranularity granularity) {
    Location loc = parseLatLong(location);
    if (loc != null) {
      return loc;
    }
    if (!isEnabled()) {
      return null;
    }
    return lookup(location, granularity);
  }

  @Override
  public String reverseLookup(Location location, String defaultValue) {
    log.log(Level.INFO, "Looking up location: {0}", location);
    JSONObject obj = googleResource.reverseLookup(location);
    log.log(Level.INFO, "Reverse lookup results for {0}: \n{1}",
        new Object[] {location, obj});
    checkStatus(obj);
    try {
      JSONArray results = obj.getJSONArray("results");
      if (results.length() > 0) {
        JSONObject firstResult = results.getJSONObject(0);
        return firstResult.getString("formatted_address");
      }
    } catch (JSONException jsonException) {
      log.log(Level.WARNING, jsonException.getMessage(), jsonException);
    }
    return defaultValue;
  }

  private void checkStatus(JSONObject result) {
    if ("OVER_QUERY_LIMIT".equals(result.optString("status"))) {
      throw new OverQueryLimitException();
    }
  }

  private boolean isEnabled() {
    // TODO: use configuration, not system property
    return "true".equals(System.getProperty("google.geolocator.enabled", "true"));
  }

  private @Nullable Location lookup(String location, GeolocationGranularity granularity) {
    JSONObject obj = googleResource.findLocation(location);
    try {
      log.log(Level.INFO, "Geolocation result for {0}: \n{1}",
          new Object[] {location, obj.toString()});
      JSONArray results = obj.getJSONArray("results");
      checkStatus(obj);
      if (results.length() > 0) {
        final JSONObject firstResult = results.getJSONObject(0);
        final JSONArray types = firstResult.getJSONArray("types");
        if (arrayContains(types, "locality")) {
          log.log(Level.INFO, "Result was too granular");
          return null;
        }
        final JSONObject geometry = firstResult.getJSONObject("geometry");
        String locationType = geometry.optString("location_type", null);
        if (granularity == GeolocationGranularity.NARROW && ("GEOMETRIC_CENTER".equals(locationType)
            || "APPROXIMATE".equals(locationType))) {
          // TODO: inefficient
          if (arrayContains(types, "point_of_interest") || arrayContains(types, "establishment")) {
            log.log(Level.INFO, "Location type was: " + locationType);
            return null;
          }
        }
        JSONObject loc =
            geometry.getJSONObject("location");
        return Location.builder().lat(loc.getDouble("lat")).lng(loc.getDouble("lng")).name(location)
            .build();
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
      return Location.builder()
          .lat(Double.parseDouble(m.group(1)))
          .lng(Double.parseDouble(m.group(2))).name(name).build();
    }
    return null;
  }
}
