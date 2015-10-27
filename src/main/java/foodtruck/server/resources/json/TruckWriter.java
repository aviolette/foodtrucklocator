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

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Truck;
import foodtruck.server.resources.BadRequestException;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
@Provider
public class TruckWriter implements JSONWriter<Truck>, MessageBodyWriter<Truck> {
  private final AbbreviatedTruckWriter truckWriter;

  @Inject
  public TruckWriter(AbbreviatedTruckWriter truckWriter) {
    this.truckWriter = truckWriter;
  }

  @Override public JSONObject asJSON(Truck truck) throws JSONException {
    JSONObject obj = truckWriter.asJSON(truck);
    obj.put("description", truck.getDescription());
    obj.put("previewIcon", truck.getPreviewIcon());
    obj.put("inactive", truck.isInactive());
    Truck.Stats stats = truck.getStats();
    if (stats != null) {
      obj.put("firstSeen", stats.getFirstSeen() == null ? 0 : stats.getFirstSeen().getMillis());
      obj.put("lastSeen", stats.getLastSeen() == null ? 0 : stats.getLastSeen().getMillis());
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
      throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(truck), entityStream);
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }
}