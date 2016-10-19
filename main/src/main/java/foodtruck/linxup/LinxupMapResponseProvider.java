package foodtruck.linxup;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.javadocmd.simplelatlng.LatLng;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import foodtruck.server.resources.json.JSONSerializer;

/**
 * @author aviolette
 * @since 7/25/16
 */
@SuppressWarnings("WeakerAccess")
public class LinxupMapResponseProvider implements MessageBodyReader<LinxupMapResponse> {

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(LinxupMapResponse.class);
  }

  @Override
  public LinxupMapResponse readFrom(Class<LinxupMapResponse> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONObject obj = JSONSerializer.readJSON(entityStream);
      if ("Error".equals(obj.getString("responseType"))) {
        JSONObject errorObject = obj.getJSONObject("error");
        return new LinxupMapResponse(errorObject.getString("type"), errorObject.getString("message"));
      } else {
        JSONArray positionsArr = obj.getJSONObject("data").getJSONArray("positions");
        ImmutableList.Builder<Position> positions = ImmutableList.builder();
        for (int i=0; i < positionsArr.length(); i++) {
          JSONObject posObj = positionsArr.getJSONObject(i);
          double lat = posObj.getDouble("latitude"), lng = posObj.getDouble("longitude");
          Position.Builder positionBuilder = Position.builder()
              .date(new DateTime(posObj.getLong("date")))
              .direction(posObj.getInt("direction"))
              .speedMph(posObj.getInt("speed"))
              .speeding(posObj.optBoolean("speeding"))
              .estimatedSpeedLimit(posObj.optInt("estSpeedLimit"))
              .latLng(new LatLng(lat, lng))
              .driverId(posObj.optString("driverId"))
              .deviceNumber(posObj.optString("deviceNbr"))
              .simDeviceNumber(posObj.optString("simDeviceNumber"))
              .deviceTypeDescription(posObj.optString("deviceTypeDescription"))
              .vehicleLabel(posObj.getString("label"));
          positions.add(positionBuilder.build());
        }
        return new LinxupMapResponse(positions.build());
      }
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
