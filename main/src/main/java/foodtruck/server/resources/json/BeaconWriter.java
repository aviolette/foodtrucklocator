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
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.TrackingDevice;
import foodtruck.server.resources.BadRequestException;
import foodtruck.time.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 8/9/16
 */
@Provider
public class BeaconWriter implements MessageBodyWriter<TrackingDevice>, JSONWriter<TrackingDevice> {

  private final LocationWriter locationWriter;
  private final DateTimeFormatter formatter;

  @Inject
  public BeaconWriter(LocationWriter locationWriter, @FriendlyDateTimeFormat DateTimeFormatter formatter) {
    this.locationWriter = locationWriter;
    this.formatter = formatter;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(TrackingDevice.class);
  }

  @Override
  public long getSize(TrackingDevice trackingDevice, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(TrackingDevice trackingDevice, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(trackingDevice), entityStream);
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }

  public JSONObject asJSON(TrackingDevice trackingDevice) throws JSONException {
    return new JSONObject().put("truckOwnerId", trackingDevice.getTruckOwnerId())
        .put("deviceNumber", trackingDevice.getDeviceNumber())
        .put("lastLocation", locationWriter.asJSON(trackingDevice.getLastLocation()))
        .put("label", trackingDevice.getLabel())
        .put("direction", trackingDevice.getDegreesFromNorth())
        .put("enabled", trackingDevice.isEnabled())
        .put("hasWarning", trackingDevice.isHasWarning())
        .put("warning", trackingDevice.getWarning())
        .put("parked", trackingDevice.isParked())
        .put("blacklisted", trackingDevice.isAtBlacklistedLocation())
        .put("id", trackingDevice.getKey())
        .put("battery", trackingDevice.getBatteryCharge())
        .put("fuelLevel", trackingDevice.getFuelLevel())
        .put("lastSpeedInMPH", trackingDevice.getLastSpeedInMPH())
        .put("lastModified", formatter.print(trackingDevice.getLastModified()))
        .put("lastBroadcast", formatter.print(trackingDevice.getLastBroadcast()));
  }
}
