package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Location;
import foodtruck.server.resources.BadRequestException;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */

@Provider
public class LocationWriter implements JSONWriter<Location>, MessageBodyWriter<Location> {
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

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(Location.class);
  }

  @Override public long getSize(Location location, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Location location, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(location), entityStream);
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }

  }
}