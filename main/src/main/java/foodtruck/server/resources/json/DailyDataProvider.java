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

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.DailyDataDAO;
import foodtruck.model.DailyData;
import foodtruck.server.resources.BadRequestException;
import foodtruck.time.DateOnlyFormatter;

import static foodtruck.server.resources.json.JSONSerializer.readJSON;

/**
 * @author aviolette
 * @since 4/15/16
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class DailyDataProvider implements MessageBodyReader<DailyData> {
  private final DailyDataDAO dao;
  private final DateTimeFormatter formatter;

  @Inject
  public DailyDataProvider(DailyDataDAO dailyDataDAO, @DateOnlyFormatter DateTimeFormatter formatter) {
    this.dao = dailyDataDAO;
    this.formatter = formatter;
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(DailyData.class);
  }

  @Override
  public DailyData readFrom(Class<DailyData> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {
    try {
      return asJSON(readJSON(entityStream));
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }

  public DailyData asJSON(JSONObject json) throws JSONException {
    String truckId = json.getString("truckId");
    LocalDate date = formatter.parseLocalDate(json.getString("date"));
    DailyData original = dao.findByTruckAndDay(truckId, date);
    DailyData.Builder builder = DailyData.builder();
    if (original != null) {
      builder.key(original.getKey());
    }
    builder.truckId(truckId);
    builder.onDate(date);
    JSONArray arr = json.getJSONArray("specials");
    for (int i = 0; i < arr.length(); i++) {
      JSONObject special = arr.getJSONObject(i);
      builder.addSpecial(special.getString("special"), false);
    }
    return builder.build();
  }
}
