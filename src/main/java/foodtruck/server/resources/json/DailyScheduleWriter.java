package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.server.resources.BadRequestException;
import foodtruck.util.DateOnlyFormatter;
import foodtruck.util.TimeOnlyFormatter;

/**
 * @author aviolette
 * @since 1/28/13
 */
@Provider @Produces(MediaType.APPLICATION_JSON)
public class DailyScheduleWriter implements MessageBodyWriter<DailySchedule>, JSONWriter<DailySchedule> {
  private final LocationWriter locationWriter;
  private final DateTimeFormatter formatter;
  private final TruckWriter truckWriter;
  private final DateTimeFormatter dateOnlyFormatter;

  @Inject
  public DailyScheduleWriter(LocationWriter locationWriter,@TimeOnlyFormatter DateTimeFormatter formatter,
      TruckWriter truckWriter, @DateOnlyFormatter DateTimeFormatter dateOnlyFormatter) {
    this.locationWriter = locationWriter;
    this.formatter = formatter;
    this.truckWriter = truckWriter;
    this.dateOnlyFormatter = dateOnlyFormatter;
  }

  @Override public JSONObject asJSON(DailySchedule schedule) throws JSONException {
    JSONObject payload = new JSONObject();
    Iterable<Truck> trucks =
        Iterables.transform(schedule.getStops(), new Function<TruckStop, Truck>() {
          @Override public Truck apply(TruckStop input) {
            return input.getTruck();
          }
        });
    Map<Location, Integer> locations = Maps.newHashMap();
    JSONArray locationArr = new JSONArray();
    int i = 1;
    for (TruckStop stop : schedule.getStops()) {
      locations.put(stop.getLocation(), i++);
      locationArr.put(locationWriter.writeLocation(stop.getLocation(), i, false));
    }
    JSONArray schedules = new JSONArray();
    for (TruckStop stop : schedule.getStops()) {
      JSONObject truckStop = new JSONObject()
          .put("location", locations.get(stop.getLocation()))
          .put("truckId", stop.getTruck().getId())
          .put("startTime", formatter.print(stop.getStartTime()))
          .put("startMillis", stop.getStartTime().getMillis())
          .put("endMillis", stop.getEndTime().getMillis())
          .put("endTime", formatter.print(stop.getEndTime()));
      schedules.put(truckStop);
    }
    payload.put("trucks", JSONSerializer.buildArray(ImmutableSet.copyOf(trucks), truckWriter));
    payload.put("locations", locationArr);
    payload.put("stops", schedules);
    payload.put("date", dateOnlyFormatter.print(schedule.getDay()));
    return payload;
  }

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(DailySchedule.class);
  }

  @Override public long getSize(DailySchedule dailySchedule, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override public void writeTo(DailySchedule dailySchedule, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(dailySchedule), entityStream);
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }

  }
}
