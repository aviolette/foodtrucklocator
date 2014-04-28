package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Truck;
import foodtruck.server.resources.BadRequestException;
import static foodtruck.server.resources.json.JSONSerializer.readJSON;

/**
 * @author aviolette@gmail.com
 * @since 6/16/12
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class TruckReader implements MessageBodyReader<Truck> {
  @Override public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type.equals(Truck.class);
  }

  @Override public Truck readFrom(Class<Truck> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      return asJSON(readJSON(entityStream));
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }

  public Truck asJSON(JSONObject json) throws JSONException {
    return Truck.builder()
        .id(json.getString("id"))
        .description(json.optString("description"))
        .facebook(json.optString("facebook"))
        .facebookPageId(json.optString("facebookPageId"))
        .foursquareUrl(json.optString("foursquare"))
        .iconUrl(json.optString("iconUrl"))
        .previewIcon(json.optString("previewIcon"))
        .yelpSlug(json.optString("yelp"))
        .name(json.getString("name"))
        .twitterHandle(json.getString("twitterHandle")).build();

  }
}
