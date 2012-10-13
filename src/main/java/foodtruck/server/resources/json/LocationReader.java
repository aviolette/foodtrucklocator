package foodtruck.server.resources.json;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since 10/11/12
 */
public class LocationReader  {
  public Location toLocation(JSONObject obj) throws JSONException {
    double lat = obj.getDouble("latitude");
    double lng = obj.getDouble("longitude");
    boolean valid = obj.optBoolean("valid", true);
    String name = obj.getString("name");
    long key = obj.optLong("key", 0);
    return Location.builder().lat(lat).lng(lng).name(name).key((key > 0) ? key : null)
        .url(obj.optString("url")).description(obj.optString("description")).valid(valid).build();
  }
}
