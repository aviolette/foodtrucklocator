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

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStop;
import foodtruck.server.resources.BadRequestException;

/**
 * @author aviolette@gmail.com
 * @since 10/15/12
 */
@Provider @Produces(MediaType.APPLICATION_JSON)
public class TruckScheduleWriter implements MessageBodyWriter<TruckSchedule> {
  private final LocationWriter locationWriter;
  private final TruckWriter truckWriter;
  private final DateTimeFormatter timeFormatter;

  @Inject
  public TruckScheduleWriter(TruckWriter truckWriter, LocationWriter locationWriter,
      DateTimeZone zone) {
    this.truckWriter = truckWriter;
    this.locationWriter = locationWriter;
    timeFormatter = DateTimeFormat.forPattern("hh:mm a").withZone(zone);

  }

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type.equals(TruckSchedule.class);
  }

  @Override public long getSize(TruckSchedule truckSchedule, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override public void writeTo(TruckSchedule truckSchedule, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(truckSchedule), entityStream);
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }

  public JSONObject asJSON(TruckSchedule schedule) throws JSONException {
    JSONObject obj = new JSONObject()
        .put("truck", truckWriter.asJSON(schedule.getTruck()))
        .put("day", schedule.getDate().toString());
    JSONArray arr = new JSONArray();
    for (TruckStop stop : schedule.getStops()) {
      JSONObject truckStop = new JSONObject()
          .put("location", locationWriter.writeLocation(stop.getLocation(), 0, false))
          .put("id", stop.getKey())
          .put("locked", stop.isLocked())
          .put("startTimeMillis", stop.getStartTime().getMillis())
          .put("endTimeMillis", stop.getEndTime().getMillis())
          .put("startTime", timeFormatter.print(stop.getStartTime()))
          .put("endTime", timeFormatter.print(stop.getEndTime()));
      arr.put(truckStop);
    }
    return obj.put("stops", arr);
  }
}
