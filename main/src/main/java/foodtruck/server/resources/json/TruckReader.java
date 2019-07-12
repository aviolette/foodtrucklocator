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

import com.google.common.collect.ImmutableSet;

import org.codehaus.jettison.json.JSONArray;
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
  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(Truck.class);
  }

  @Override
  public Truck readFrom(Class<Truck> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {
    try {
      return asJSON(readJSON(entityStream));
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }

  public Truck asJSON(JSONObject json) throws JSONException {
    Truck.Builder builder = Truck.builder()
        .id(json.getString("id")
            .toLowerCase())
        .description(json.optString("description"))
        .facebook(json.optString("facebook"))
        .facebookPageId(json.optString("facebookPageId"))
        .iconUrl(json.optString("iconUrl"))
        .previewIcon(json.optString("previewIcon"))
        .yelpSlug(json.optString("yelp"))
        .name(json.getString("name"))
        .twitterHandle(json.getString("twitterHandle")
            .toLowerCase());
    JSONArray arr = json.optJSONArray("categories");
    if (arr != null) {
      ImmutableSet.Builder<String> catBuilder = ImmutableSet.builder();
      for (int i = 0; i < arr.length(); i++) {
        catBuilder.add(arr.getString(i));
      }
      builder.categories(catBuilder.build());
    }
    return builder.build();
  }
}
