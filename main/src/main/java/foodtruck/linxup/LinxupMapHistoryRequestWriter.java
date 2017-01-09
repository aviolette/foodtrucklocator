package foodtruck.linxup;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.server.resources.json.JSONSerializer;

/**
 * @author aviolette
 * @since 7/25/16
 */
public class LinxupMapHistoryRequestWriter implements MessageBodyWriter<LinxupMapHistoryRequest> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(LinxupMapHistoryRequest.class);
  }

  @Override
  public long getSize(LinxupMapHistoryRequest tripRequest, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(LinxupMapHistoryRequest request, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(new JSONObject().put("username", request.getUserName())
          .put("fromDate", request.getStart()
              .getMillis())
          .put("toDate", request.getEnd()
              .getMillis())
          .put("imei", request.getDeviceId())
          .put("password", request.getPassword()), entityStream);
    } catch (JSONException e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }
  }
}
