package foodtruck.server.resources.text;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;

import foodtruck.model.Truck;
import foodtruck.server.resources.json.JSONSerializer;

/**
 * @author aviolette
 * @since 9/7/16
 */
@Provider
@Produces(MediaType.TEXT_PLAIN)
public class TruckTextCollectionWriter implements MessageBodyWriter<Iterable<Truck>> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return JSONSerializer.isParameterizedCollectionOf(genericType, type, Truck.class);
  }

  @Override
  public long getSize(Iterable<Truck> trucks, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Iterable<Truck> trucks, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    entityStream.write(FluentIterable.from(trucks)
        .transform(Truck.TO_NAME)
        .join(Joiner.on("\n"))
        .getBytes("UTF-8"));
  }
}
