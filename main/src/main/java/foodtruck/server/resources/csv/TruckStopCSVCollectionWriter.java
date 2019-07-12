package foodtruck.server.resources.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVWriter;
import foodtruck.model.TruckStop;
import foodtruck.server.resources.json.JSONSerializer;
import foodtruck.time.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 11/29/14
 */
@Provider
@Produces("text/csv")
public class TruckStopCSVCollectionWriter implements MessageBodyWriter<Iterable<TruckStop>> {
  private static final String[] HEADER = new String[]{"START TIME", "END TIME", "TRUCK NAME", "LOCATION NAME", "LATITUDE", "LONGITUDE"};
  private final DateTimeFormatter format;

  @Inject
  public TruckStopCSVCollectionWriter(@FriendlyDateTimeFormat DateTimeFormatter format) {
    this.format = format;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return JSONSerializer.isParameterizedCollectionOf(genericType, type, TruckStop.class);
  }

  @Override
  public long getSize(Iterable<TruckStop> truckStops, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Iterable<TruckStop> truckStops, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {

    final StringWriter stringWriter = new StringWriter();
    CSVWriter writer = new CSVWriter(stringWriter);
    writer.writeNext(HEADER);
    for (TruckStop truckStop : truckStops) {
      writer.writeNext(truckEntries(truckStop));
    }
    writer.close();
    entityStream.write(stringWriter.getBuffer()
        .toString()
        .getBytes("UTF-8"));
  }

  private String[] truckEntries(TruckStop truckStop) {
    List<String> entries = Lists.newArrayListWithCapacity(HEADER.length);
    entries.add(format.print(truckStop.getStartTime()));
    entries.add(format.print(truckStop.getEndTime()));
    entries.add(truckStop.getTruck()
        .getName());
    entries.add(truckStop.getLocation()
        .getName());
    entries.add(String.format("%f", truckStop.getLocation()
        .getLatitude()));
    entries.add(String.format("%f", truckStop.getLocation()
        .getLongitude()));
    return entries.toArray(new String[entries.size()]);
  }
}
