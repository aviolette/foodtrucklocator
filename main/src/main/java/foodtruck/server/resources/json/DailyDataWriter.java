package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.DailyData;
import foodtruck.server.resources.BadRequestException;
import foodtruck.time.DateOnlyFormatter;

/**
 * @author aviolette
 * @since 12/16/15
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class DailyDataWriter implements MessageBodyWriter<DailyData>, JSONWriter<DailyData> {
  private final DateTimeFormatter formatter;

  @Inject
  public DailyDataWriter(@DateOnlyFormatter DateTimeFormatter formatter) {
    this.formatter = formatter;
  }

  @Override
  public JSONObject asJSON(DailyData dailyData) throws JSONException {
    return new JSONObject().put("truckId", dailyData.getTruckId())
        .put("date", formatter.print(dailyData.getOnDate()))
        .put("specials", FluentIterable.from(dailyData.getSpecials())
            .transform(new Function<DailyData.SpecialInfo, JSONObject>() {
              public JSONObject apply(DailyData.SpecialInfo input) {
                try {
                  return new JSONObject().put("special", input.getSpecial())
                      .put("soldout", input.isSoldOut());
                } catch (JSONException e) {
                  throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
                }
              }
            })
            .toList());
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return DailyData.class.equals(type);
  }

  @Override
  public long getSize(DailyData dailyData, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(DailyData dailyData, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(dailyData), entityStream);
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }
}
