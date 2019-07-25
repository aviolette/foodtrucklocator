package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.annotations.UseJackson;
import foodtruck.model.Truck;
import foodtruck.server.resources.BadRequestException;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
@Provider
public class TruckWriter implements JSONWriter<Truck>, MessageBodyWriter<Truck> {
  private final AbbreviatedTruckWriter truckWriter;
  private final ObjectMapper objectMapper;

  @Inject
  public TruckWriter(AbbreviatedTruckWriter truckWriter, ObjectMapper mapper) {
    this.truckWriter = truckWriter;
    this.objectMapper = mapper;
  }

  @Override
  public JSONObject asJSON(Truck truck) throws JSONException {
    JSONObject obj = truckWriter.asJSON(truck);
    obj.put("previewIcon", truck.getPreviewIcon());
    obj.put("inactive", truck.isInactive());
    obj.put("menuUrl", truck.getMenuUrl());
    Truck.Stats stats = truck.getStats();
    if (stats != null) {
      obj.put("firstSeen", stats.getFirstSeen() == null ? 0 : stats.getFirstSeen()
          .getMillis());
      if (stats.getWhereFirstSeen() != null) {
        obj.put("whereFirstSeen", stats.getWhereFirstSeen()
            .getName());
      }
      obj.put("lastSeen", stats.getLastSeen() == null ? 0 : stats.getLastSeen()
          .getMillis());
      if (stats.getWhereLastSeen() != null) {
        obj.put("whereLastSeen", stats.getWhereLastSeen()
            .getName());
      }
    }
    return obj;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Truck.class.equals(type);
  }

  @Override
  public long getSize(Truck truck, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Truck truck, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    if (Arrays.stream(annotations).anyMatch(annotation -> annotation.annotationType() == UseJackson.class)) {
      objectMapper.writeValue(entityStream, truck);
    } else {
      try {
        JSONSerializer.writeJSON(asJSON(truck), entityStream);
      } catch (JSONException e) {
        throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
      }
    }
  }
}