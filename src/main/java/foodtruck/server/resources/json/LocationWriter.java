package foodtruck.server.resources.json;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
public class LocationWriter implements JSONWriter<Location> {
  @Override public JSONObject asJSON(Location location) throws JSONException {
    return writeLocation(location, 0, false);
  }

  public JSONObject writeLocation(Location location, int id, boolean fullOptions)
      throws JSONException {
    JSONObject obj = new JSONObject()
        .put("latitude", location.getLatitude())
        .put("longitude", location.getLongitude())
        .put("radius", location.getRadius())
        .put("name", location.getName());
    if (fullOptions) {
      obj.put("valid", location.isValid());
      obj.put("key", location.getKey());
    }
    return (id != 0) ? obj.put("id", id) : obj;
  }
}