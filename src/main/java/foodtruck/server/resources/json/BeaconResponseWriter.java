package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.beaconnaise.BeaconResponse;

/**
 * @author aviolette
 * @since 7/29/13
 */
@Provider @Produces(MediaType.APPLICATION_JSON)
public class BeaconResponseWriter implements MessageBodyWriter<BeaconResponse>, JSONWriter<BeaconResponse> {
  private final TruckStopWriter writer;

  @Inject
  public BeaconResponseWriter(TruckStopWriter writer) {
    this.writer = writer;
  }

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(BeaconResponse.class);
  }

  @Override
  public long getSize(BeaconResponse beaconResponse, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(BeaconResponse beaconResponse, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(beaconResponse), entityStream);
    } catch (JSONException e) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  @Override public JSONObject asJSON(BeaconResponse beaconResponse) throws JSONException {
    JSONObject obj = new JSONObject();
    obj.put("stop", writer.asJSON(beaconResponse.getStop()));
    return obj;
  }
}
