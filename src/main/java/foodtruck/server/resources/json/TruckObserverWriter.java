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

import com.google.common.base.Joiner;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.TruckObserver;
import foodtruck.server.resources.BadRequestException;

/**
 * @author aviolette
 * @since 6/10/13
 */
@Provider
public class TruckObserverWriter implements MessageBodyWriter<TruckObserver>, JSONWriter<TruckObserver> {
  private final LocationWriter locationWriter;

  @Inject
  public TruckObserverWriter(LocationWriter locationWriter) {
    this.locationWriter = locationWriter;
  }

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(TruckObserver.class);
  }

  @Override public long getSize(TruckObserver truckObserver, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override public void writeTo(TruckObserver truckObserver, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(truckObserver), entityStream);
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }

  public JSONObject asJSON(TruckObserver truckObserver) throws JSONException {
    return new JSONObject()
        .put("twitterHandle", truckObserver.getTwitterHandle())
        .put("keywords", Joiner.on(",").join(truckObserver.getKeywords()))
        .put("location", locationWriter.asJSON(truckObserver.getLocation()));
  }
}
