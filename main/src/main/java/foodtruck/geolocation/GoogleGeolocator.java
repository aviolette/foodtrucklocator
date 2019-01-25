package foodtruck.geolocation;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientHandlerException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.appengine.cache.MemcacheCacher;
import foodtruck.model.Location;
import foodtruck.monitoring.Monitored;
import foodtruck.util.ServiceException;

/**
 * GeoLocator that uses google geocoding.
 * @author aviolette@gmail.com
 * @since 8/29/11
 */
class GoogleGeolocator implements GeoLocator {
  private static final Logger log = Logger.getLogger(GoogleGeolocator.class.getName());
  private static final String REVERSE_LOOKUP_BACKOFF = "GoogleGeolocator-QueryLimit-Exceeded";
  private final Pattern latLongExpression;
  private final GoogleResource googleResource;
  private final Set<String> NARROW_SET = ImmutableSet.of("intersection", "street_address", "premise"),
                BROAD_SET = ImmutableSet.of("intersection", "street_address", "airport", "park", "point_of_interest", "premise");
  private final MemcacheCacher cacher;

  @Inject
  public GoogleGeolocator(GoogleResource googleResource, MemcacheCacher cacher) {
    latLongExpression = Pattern.compile("([\\-|\\d|\\.]+),\\s*([\\-|\\d|\\.]+)[\\s*,\\s*]?");
    this.googleResource = googleResource;
    this.cacher = cacher;
  }

  @Override @Monitored
  public Location locate(String location, GeolocationGranularity granularity)
      throws ServiceException {
    Location loc = parseLatLong(location);
    if (loc != null) {
      return loc;
    }
    return lookup(location, granularity);
  }

  @Override
  public Optional<Location> locateOpt(String location) {
    return Optional.ofNullable(locate(location, GeolocationGranularity.NARROW));
  }

  @Override @Monitored
  public @Nullable Location reverseLookup(Location location) throws ServiceException {
    try {
      log.log(Level.INFO, "Looking up location: {0}", location);
      if (cacher.get(REVERSE_LOOKUP_BACKOFF) == Boolean.TRUE) {
        log.log(Level.WARNING, "Query limit exceeded so not performing lookup until cache clear");
        throw new OverQueryLimitException();
      }
      JSONObject obj = googleResource.reverseLookup(location);
      log.log(Level.FINE, "Reverse lookup results for {0}: \n{1}",
          new Object[] {location, obj});
      checkStatus(obj);
      JSONArray results = obj.getJSONArray("results");
      if (results.length() > 0) {
        JSONObject firstResult = results.getJSONObject(0);
        Location.Builder builder = new Location.Builder(location)
            .name(addressFromJson(firstResult))
            .valid(true);
        localityInformation(builder, firstResult);
        return builder.build();
      }
    } catch (ClientHandlerException|JSONException ex) {
      throw new ServiceException(ex);
    }
    return null;
  }

  private void localityInformation(Location.Builder locationBuilder, JSONObject result) throws JSONException {
    final JSONArray addressComponents = result.getJSONArray("address_components");
    if (addressComponents != null) {
      for (int i=0; i < addressComponents.length(); i++) {
        JSONObject component = addressComponents.getJSONObject(i);
        JSONArray addressTypes = component.getJSONArray("types");
        if (arrayContains(addressTypes, ImmutableSet.of("locality"))) {
          locationBuilder.city(component.getString("long_name"));
        } else if (arrayContains(addressTypes, ImmutableSet.of("neighborhood"))) {
          locationBuilder.neighborhood(component.getString("long_name"));
        }
      }
    }
  }

  private String addressFromJson(JSONObject firstResult) {
    return Strings.nullToEmpty(firstResult.optString("formatted_address"));
  }

  private void checkStatus(JSONObject result) throws OverQueryLimitException {
    if ("OVER_QUERY_LIMIT".equals(result.optString("status"))) {
      cacher.put(REVERSE_LOOKUP_BACKOFF, Boolean.TRUE, 5);
      throw new OverQueryLimitException();
    }
  }

  private @Nullable Location lookup(String location, GeolocationGranularity granularity)
      throws ServiceException {
    log.log(Level.INFO, "Address lookup: {0}", location);
    JSONObject obj = googleResource.findLocation(location);
    try {
      log.log(Level.INFO, "Geolocation result for {0}: \n{1}",
          new Object[] {location, obj.toString()});
      JSONArray results = obj.getJSONArray("results");
      checkStatus(obj);
      if (results.length() > 0) {
        final JSONObject firstResult = results.getJSONObject(0);
        final JSONArray types = firstResult.getJSONArray("types");

        if (!arrayContains(types, granularity == GeolocationGranularity.BROAD ? BROAD_SET : NARROW_SET)) {
          log.log(Level.INFO, "Result was too granular");
          return null;
        }

        final JSONObject geometry = firstResult.getJSONObject("geometry");
        JSONObject loc = geometry.getJSONObject("location");
        Location.Builder builder = Location.builder()
            .description(addressFromJson(firstResult))
            .lat(loc.getDouble("lat"))
            .lng(loc.getDouble("lng"))
            .name(location);
        localityInformation(builder, firstResult);
        return builder.build();
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  private boolean arrayContains(@Nullable JSONArray arr, Set<String> searchWords) throws JSONException {
    if (arr == null) {
      return false;
    }
    for (int i = 0; i < arr.length(); i++) {
      String str = arr.getString(i);
      for (String searchWord : searchWords) {
        if (searchWord.equals(str)) {
          return true;
        }
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
