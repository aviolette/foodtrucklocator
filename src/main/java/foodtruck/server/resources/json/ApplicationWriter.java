package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Application;
import foodtruck.server.resources.BadRequestException;

/**
 * @author aviolette
 * @since 1/25/13
 */
@Provider @Produces(MediaType.APPLICATION_JSON)
public class ApplicationWriter implements JSONWriter<Application>, MessageBodyWriter<Application> {
  @Override public JSONObject asJSON(Application application) throws JSONException {
    return new JSONObject()
        .put("name", application.getName())
        .put("appKey", application.getAppKey())
        .put("enabled", application.isEnabled())
        .put("description", application.getDescription());
  }

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(Application.class);
  }

  @Override public long getSize(Application application, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override public void writeTo(Application application, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(application), entityStream);
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }
}
