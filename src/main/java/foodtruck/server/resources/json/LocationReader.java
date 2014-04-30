package foodtruck.server.resources.json;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since 10/11/12
 */
public class LocationReader {
  public Location toLocation(JSONObject obj) throws JSONException {
    long key = obj.optLong("key", 0);
    return Location.builder()
        .lat(obj.getDouble("latitude"))
        .lng(obj.getDouble("longitude"))
        .alias(obj.optString("alias"))
        .twitterHandle(obj.optString("twitterHandle"))
        .popular(obj.optBoolean("popular", false))
        .designatedStop(obj.optBoolean("designatedStop", false))
        .autocomplete(obj.optBoolean("autocomplete", false))
        .radius(obj.getDouble("radius"))
        .name(obj.getString("name")).key((key > 0) ? key : null)
        .url(obj.optString("url"))
        .description(obj.optString("description"))
        .valid(obj.optBoolean("valid", true)).build();
  }
}
