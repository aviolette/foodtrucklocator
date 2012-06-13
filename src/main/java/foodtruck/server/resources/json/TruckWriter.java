package foodtruck.server.resources.json;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
public class TruckWriter implements JSONWriter<Truck> {
  @Override public JSONObject asJSON(Truck truck) throws JSONException {
    return new org.codehaus.jettison.json.JSONObject()
        .put("id", truck.getId())
        .put("description", truck.getDescription())
        .put("iconUrl", truck.getIconUrl())
        .put("twitterHandle", truck.getTwitterHandle())
        .put("facebook", truck.getFacebook())
        .put("foursquare", truck.getFoursquareUrl())
        .put("name", truck.getName())
        .put("inactive", truck.isInactive())
        .put("url", truck.getUrl());
  }
}