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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.DailyData;
import foodtruck.model.LocationWithDailyData;
import foodtruck.server.resources.BadRequestException;
import foodtruck.time.DateOnlyFormatter;

/**
 * @author aviolette
 * @since 10/27/15
 */
@Provider
public class LocationWithDailyDataWriter implements JSONWriter<LocationWithDailyData>, MessageBodyWriter<LocationWithDailyData> {
  private final LocationWriter locationWriter;
  private final DateTimeFormatter dateOnlyFormatter;

  @Inject
  public LocationWithDailyDataWriter(LocationWriter locationWriter,
      @DateOnlyFormatter DateTimeFormatter dateOnlyFormatter) {
    this.locationWriter = locationWriter;
    this.dateOnlyFormatter = dateOnlyFormatter;
  }

  @Override
  public JSONObject asJSON(LocationWithDailyData locationWithDailyData) throws JSONException {
    JSONObject jsonObject = locationWriter.asJSON(locationWithDailyData.getLocation());
    if (locationWithDailyData.getDailyData() != null) {
      JSONObject specialsJson = new JSONObject();
      specialsJson.put("forDate", dateOnlyFormatter.print(locationWithDailyData.getDailyData()
          .getOnDate()));
      JSONArray specials = new JSONArray();
      for (DailyData.SpecialInfo special : locationWithDailyData.getDailyData()
          .getSpecials()) {
        JSONObject obj = new JSONObject().put("special", special.getSpecial())
            .put("soldOut", special.isSoldOut());
        specials.put(obj);

      }
      specialsJson.put("specials", specials);
      jsonObject.put("specials", specialsJson);
    }
    return jsonObject;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(LocationWithDailyData.class);
  }

  @Override
  public long getSize(LocationWithDailyData locationWithDailyData, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(LocationWithDailyData locationWithDailyData, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(locationWithDailyData), entityStream);
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }
}
