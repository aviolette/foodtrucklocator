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

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.util.Clock;
import foodtruck.util.TimeOnlyFormatter;

/**
 * @author aviolette@gmail.com
 * @since 10/11/12
 */
@Provider @Consumes(MediaType.APPLICATION_JSON)
public class TruckStopReader implements MessageBodyReader<TruckStop> {
  private final TruckDAO truckDAO;
  private final Clock clock;
  private final DateTimeFormatter format;
  private final GeoLocator geolocator;
  private final LocationReader locationReader;

  @Inject
  public TruckStopReader(Clock clock, TruckDAO trucks, GeoLocator geolocator,
      LocationReader locationReader, @TimeOnlyFormatter DateTimeFormatter formatter) {
    this.truckDAO = trucks;
    this.clock = clock;
    this.format = formatter;
    this.geolocator = geolocator;
    this.locationReader = locationReader;
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type.equals(TruckStop.class);
  }

  @Override
  public TruckStop readFrom(Class<TruckStop> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    final String json = new String(ByteStreams.toByteArray(entityStream));
    try {
      JSONObject obj = new JSONObject(json);
      Truck truck = truckDAO.findById(obj.getString("truckId"));
      checkNotNull(truck);
      LocalDate today = clock.currentDay();
      DateTime startTime = format.parseDateTime(obj.getString("startTime"))
          .withDate(today.getYear(), today.getMonthOfYear(), today.getDayOfMonth());
      DateTime endTime = format.parseDateTime(obj.getString("endTime"))
          .withDate(today.getYear(), today.getMonthOfYear(), today.getDayOfMonth());
      final JSONObject loc = obj.optJSONObject("location");
      Location location;
      if (loc == null) {
        location = geolocator.locate(obj.getString("locationName"), GeolocationGranularity.NARROW);
        checkNotNull(location, "Location couldn't be resolved");
      } else {
        location = locationReader.toLocation(loc);
        checkNotNull(location, "Location is unparsable");
      }
      checkState(location != null && location.isResolved(), "Location is not resolved");
      long key = obj.optLong("id", 0);
      boolean locked = obj.optBoolean("locked", false);
      return new TruckStop(truck, startTime, endTime, location, (key > 0) ? key : null, locked);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
