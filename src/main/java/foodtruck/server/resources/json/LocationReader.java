package foodtruck.server.resources.json;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Location;
import foodtruck.model.Url;

/**
 * @author aviolette@gmail.com
 * @since 10/11/12
 */
public class LocationReader {
  public Location toLocation(JSONObject obj) throws JSONException {
    long key = obj.optLong("key", 0);
    String imageUrlValue = obj.optString("imageUrl");
    Url imageUrl = null;
    if (!Strings.isNullOrEmpty(imageUrlValue)) {
      imageUrl = new Url(imageUrlValue);
    }
    return Location.builder()
        .lat(obj.getDouble("latitude"))
        .lng(obj.getDouble("longitude"))
        .alias(obj.optString("alias"))
        .hasBooze(obj.optBoolean("hasBooze"))
        .eventCalendarUrl(obj.optString("eventUrl"))
        .closed(obj.optBoolean("closed"))
        .twitterHandle(obj.optString("twitterHandle"))
        .popular(obj.optBoolean("popular", false))
        .designatedStop(obj.optBoolean("designatedStop", false))
        .autocomplete(obj.optBoolean("autocomplete", false))
        .radius(obj.getDouble("radius"))
        .phoneNumber(obj.optString("phone"))
        .email(obj.optString("email"))
        .imageUrl(imageUrl)
        .managerEmails(ImmutableSet.copyOf(Splitter.on(",").trimResults().omitEmptyStrings().split(obj.optString("managerEmails"))))
        .facebookUri(obj.optString("facebookUri"))
        .radiateTo(obj.optInt("radiateTo", 0))
        .name(obj.getString("name")).key((key > 0) ? key : null)
        .url(obj.optString("url"))
        .ownedBy(Strings.emptyToNull(obj.optString("ownedBy")))
        .description(obj.optString("description"))
        .valid(obj.optBoolean("valid", true)).build();
  }
}
