package foodtruck.server.resources.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import au.com.bytecode.opencsv.CSVWriter;
import foodtruck.model.Truck;
import foodtruck.server.resources.json.JSONSerializer;

/**
 * @author aviolette@gmail.com
 * @since 6/13/12
 */
@Provider
@Produces("text/csv")
public class TruckCSVCollectionWriter implements MessageBodyWriter<Iterable<Truck>> {

  private static final String[] HEADER =
      new String[] {"NAME", "TWITTER HANDLE", "FACEBOOK URI", "FOURSQUARE ID", "WEBSITE URL",
          "PHONE", "EMAIL"};

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return JSONSerializer.isParameterizedCollectionOf(genericType, type, Truck.class);
  }

  @Override public long getSize(Iterable<Truck> truckIterable, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override public void writeTo(Iterable<Truck> truckIterable, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {

    final StringWriter stringWriter = new StringWriter();
    CSVWriter writer = new CSVWriter(stringWriter);
    writer.writeNext(HEADER);
    for (Truck truck : truckIterable) {
      writer.writeNext(truckEntries(truck));
    }
    writer.close();
    entityStream.write(stringWriter.getBuffer().toString().getBytes("UTF-8"));
  }

  private String[] truckEntries(Truck truck) {
    List<String> entries = Lists.newArrayListWithCapacity(8);
    entries.add(truck.getName());
    entries.add(truck.getTwitterHandle());
    entries.add(completeFacebook(truck.getFacebook()));
    entries.add(completeFoursquare(truck.getFoursquareUrl()));
    entries.add(truck.getUrl());
    entries.add(truck.getPhone());
    entries.add(truck.getEmail());
    return entries.toArray(new String[entries.size()]);
  }

  private String completeFoursquare(String foursquareUrl) {
    foursquareUrl = Strings.emptyToNull(foursquareUrl);
    if (foursquareUrl == null) {
      return null;
    }
    return "http://www.foursquare.com/v/" + foursquareUrl;
  }

  private @Nullable String completeFacebook(String facebook) {
    String uri = Strings.emptyToNull(facebook);
    if (uri == null) {
      return null;
    }
    return "http://www.facebook.com" + facebook;
  }
}
