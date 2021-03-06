package foodtruck.server.resources.json;

import java.io.IOException;
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

import foodtruck.server.resources.ErrorPayload;

/**
 * @author aviolette@gmail.com
 * @since 6/16/12
 */
@Provider
public class ErrorPayloadWriter implements MessageBodyWriter<ErrorPayload> {
  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return ErrorPayload.class.equals(type);
  }

  @Override
  public long getSize(ErrorPayload errorPayload, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(ErrorPayload errorPayload, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(new JSONObject().put("error", errorPayload.getMessage())
          .putOpt("data", errorPayload.getExtra()), entityStream);
    } catch (JSONException e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }
  }
}
