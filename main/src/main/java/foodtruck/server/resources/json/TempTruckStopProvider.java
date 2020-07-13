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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import foodtruck.model.TempTruckStop;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class TempTruckStopProvider implements MessageBodyReader<TempTruckStop> {

  private final ObjectMapper mapper;

  @Inject
  public TempTruckStopProvider(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
    return type.equals(TempTruckStop.class);
  }

  @Override
  public TempTruckStop readFrom(Class<TempTruckStop> aClass, Type type, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> multivaluedMap,
      InputStream inputStream) throws IOException, WebApplicationException {
    return mapper.readValue(inputStream, TempTruckStop.class);
  }
}
