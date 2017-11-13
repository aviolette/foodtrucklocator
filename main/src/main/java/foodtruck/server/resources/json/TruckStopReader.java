package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.google.common.html.HtmlEscapers;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.server.resources.BadRequestException;
import foodtruck.time.HtmlDateFormatter;

/**
 * @author aviolette@gmail.com
 * @since 10/11/12
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class TruckStopReader implements MessageBodyReader<TruckStop> {
  private final TruckDAO truckDAO;
  private final DateTimeFormatter format;
  private final GeoLocator geolocator;
  private final LocationReader locationReader;

  @Inject
  public TruckStopReader(TruckDAO trucks, GeoLocator geolocator, LocationReader locationReader,
      @HtmlDateFormatter DateTimeFormatter formatter) {
    this.truckDAO = trucks;
    this.format = formatter;
    this.geolocator = geolocator;
    this.locationReader = locationReader;
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(TruckStop.class);
  }

  @Override
  public TruckStop readFrom(Class<TruckStop> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {
    final String json = new String(ByteStreams.toByteArray(entityStream));
    try {
      JSONObject obj = new JSONObject(json);
      Truck truck = truckDAO.findByIdOpt(obj.getString("truckId"))
          .orElseThrow(() -> new BadRequestException("No truck specified"));
      DateTime startTime, endTime;
      try {
        startTime = format.parseDateTime(obj.getString("startTime").toUpperCase());
      } catch (IllegalArgumentException iae) {
        throw new BadRequestException("Start time incorrect or unspecified");
      }
      try {
        endTime = format.parseDateTime(obj.getString("endTime").toUpperCase());
      } catch (IllegalArgumentException iae) {
        throw new BadRequestException("Could not parse end time");
      }
      if (startTime.isAfter(endTime)) {
        throw new BadRequestException("End time is before start time");
      }
      final JSONObject loc = obj.optJSONObject("location");
      final String origin = obj.optString("origin", StopOrigin.MANUAL.toString());
      Location location;
      if (loc == null) {
        location = geolocator.locate(obj.getString("locationName"), GeolocationGranularity.NARROW);
      } else {
        location = locationReader.toLocation(loc);
      }
      if (location == null) {
        throw new BadRequestException("Location couldn't be resolved");
      }
      if (!location.isResolved()) {
        throw new BadRequestException("Location is not resolved", String.valueOf(location.getKey()));
      }
      long key = obj.optLong("id", 0);
      boolean locked = obj.optBoolean("locked", false);
      return TruckStop.builder()
          .origin(StopOrigin.valueOf(origin))
          .truck(truck)
          .description(HtmlEscapers.htmlEscaper()
              .escape(obj.optString("description")))
          .startTime(startTime)
          .imageUrl(HtmlEscapers.htmlEscaper()
              .escape(obj.optString("imageUrl")))
          .endTime(endTime)
          .location(location)
          .key((key > 0) ? key : null)
          .locked(locked)
          .build();
    } catch (JSONException e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }
  }
}
