package foodtruck.server.resources.json;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 3/19/14
 */
public class AbbreviatedTruckWriter implements JSONWriter<Truck> {
  @Override public JSONObject asJSON(Truck truck) throws JSONException {
    JSONObject obj = new JSONObject()
        .put("id", truck.getId())
        .put("iconUrl", truck.getIconUrl())
        .put("twitterHandle", truck.getTwitterHandle())
        .put("instagram", truck.getInstagramId())
        .put("facebook", truck.getFacebook())
        .put("foursquare", truck.getFoursquareUrl())
        .put("facebookPageId", truck.getFacebookPageId())
        .put("savory", truck.isSavory())
        .put("name", truck.getName())
        .put("yelp", truck.getYelpSlug())
        .put("categories", truck.publicCategories())
        .put("url", truck.getUrl());
    if (truck.isDisplayEmailPublicly()) {
      obj.put("email", truck.getEmail());
    }
    return obj;
  }
}
