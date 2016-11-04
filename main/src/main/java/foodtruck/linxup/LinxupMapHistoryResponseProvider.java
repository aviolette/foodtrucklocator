package foodtruck.linxup;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import foodtruck.model.Location;
import foodtruck.server.resources.json.JSONSerializer;

/**
 * @author aviolette
 * @since 11/1/16
 */
public class LinxupMapHistoryResponseProvider implements MessageBodyReader<LinxupMapHistoryResponse> {

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(LinxupMapHistoryResponse.class);
  }

  @Override
  public LinxupMapHistoryResponse readFrom(Class<LinxupMapHistoryResponse> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {
    JSONObject obj;
    try {
      obj = JSONSerializer.readJSON(entityStream);
      return fromJson(obj);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }

  @Nullable
  LinxupMapHistoryResponse fromJson(JSONObject obj) throws JSONException {
    if ("Error".equals(obj.getString("responseType"))) {
      JSONObject errorObject = obj.getJSONObject("error");
      return new LinxupMapHistoryResponse(errorObject.getString("type"), errorObject.getString("message"));
    } else {
      JSONObject data = obj.getJSONObject("data");
      JSONArray arr = data.getJSONArray("stops");
      ImmutableList.Builder<Stop> stopsBuilder = ImmutableList.builder();
      for (int i = 0; i < arr.length(); i++) {
        JSONObject stopObj = arr.getJSONObject(i);
        Stop stop = Stop.builder()
            .stopType(stopObj.getString("stopType"))
            .deviceId(stopObj.getString("deviceNbr"))
            .driverName(stopObj.getString("driverName"))
            .beginDate(new DateTime(stopObj.getLong("beginDate")))
            .endDate(new DateTime(stopObj.getLong("endDate")))
            .duration(Duration.standardMinutes(stopObj.optInt("duration")))
            .location(Location.builder()
                .name(stopObj.getString("street") + ", " + stopObj.getString("city") + ", " + stopObj.getString(
                    "stateCode"))
                .lat(stopObj.getDouble("latitude"))
                .lng(stopObj.getDouble("longitude"))
                .build())
            .build();
        stopsBuilder.add(stop);
      }
      ImmutableList.Builder<Position> positionBuilder = ImmutableList.builder();
      arr = data.getJSONArray("positions");
      for (int i = 0; i < arr.length(); i++) {
        positionBuilder.add(LinxupJSONHelper.readPosition(arr.getJSONObject(i)));
      }
      return new LinxupMapHistoryResponse(stopsBuilder.build(), positionBuilder.build());
    }
  }
}
