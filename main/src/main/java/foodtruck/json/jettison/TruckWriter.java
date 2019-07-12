package foodtruck.json.jettison;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
@Provider
public class TruckWriter implements JSONWriter<Truck>, MessageBodyWriter<Truck> {

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
        .put("categories", truck.getCategories())
        .put("phone", truck.getPhone())
        .put("description", truck.getDescription())
        .put("url", truck.getUrl());
    obj.put("matchOnlyIf", truck.getMatchOnlyIf());
    obj.put("doNotMatchIf", truck.getDonotMatchIf());
    obj.put("displayEmailPublicly", truck.isDisplayEmailPublicly());

    obj.put("email", truck.getEmail());
    obj.put("twittalyzer", truck.isUsingTwittalyzer());
    obj.put("previewIcon", truck.getPreviewIcon());
    obj.put("beaconnaiseEmails", truck.getBeaconnaiseEmails());
    obj.put("inactive", truck.isInactive());
    obj.put("hidden", truck.isHidden());
    obj.put("menuUrl", truck.getMenuUrl());
    obj.put("timezoneAdjustment", truck.getTimezoneAdjustment());
    obj.put("defaultCity", truck.getDefaultCity());
    obj.put("calendarUrl", truck.getCalendarUrl());
    obj.put("muteUntil", truck.getMuteUntil());
    obj.put("fullsizeImage", truck.getFullsizeImage());
    obj.put("scanFacebook", false);
    obj.put("lastScanned", "");
    obj.put("fleetSize", truck.getFleetSize());
    obj.put("backgroundImage", truck.getBackgroundImage());
    obj.put("backgroundImageLarge", truck.getBackgroundImageLarge());
    obj.put("blacklistLocationNames", truck.getBlacklistLocationNames());
    obj.put("phoneticMarkup", truck.getPhoneticMarkup());
    obj.put("phoneticAliases", truck.getPhoneticAliases());

    Truck.Stats stats = truck.getStats();
    if (stats != null) {
      obj.put("firstSeen", stats.getFirstSeen() == null ? 0 : stats.getFirstSeen().getMillis());
      if (stats.getWhereFirstSeen() != null) {
        obj.put("whereFirstSeen", stats.getWhereFirstSeen().getName());
      }
      obj.put("lastSeen", stats.getLastSeen() == null ? 0 : stats.getLastSeen().getMillis());
      if (stats.getWhereLastSeen() != null) {
        obj.put("whereLastSeen", stats.getWhereLastSeen().getName());
      }
    }
    return obj;
  }

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return Truck.class.equals(type);
  }

  @Override
  public long getSize(Truck truck, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Truck truck, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(truck), entityStream);
    } catch (JSONException e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }
  }
}