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

import com.google.common.annotations.VisibleForTesting;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.NotificationDeviceProfile;
import foodtruck.server.resources.BadRequestException;

import static foodtruck.server.resources.json.JSONSerializer.readJSON;

/**
 * @author aviolette
 * @since 2/16/16
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationDeviceProfileProvider implements MessageBodyReader<NotificationDeviceProfile> {
  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(NotificationDeviceProfile.class);
  }

  @Override
  public NotificationDeviceProfile readFrom(Class<NotificationDeviceProfile> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {
    try {
      return asJSON(readJSON(entityStream));
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }

  @VisibleForTesting
  NotificationDeviceProfile asJSON(JSONObject jsonObject) throws JSONException {
    return NotificationDeviceProfile.builder()
        .deviceToken(jsonObject.getString("deviceToken"))
        .truckIds(JSONSerializer.toStringList(jsonObject.getJSONArray("truckIds")))
        .locationNames(JSONSerializer.toStringList(jsonObject.getJSONArray("locationNames")))
        .build();
  }
}
