package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.TempTruckStop;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class TempTruckStopProvider implements MessageBodyReader<TempTruckStop> {

  private static final Logger log = Logger.getLogger(TempTruckStopProvider.class.getName());

  private final ObjectMapper mapper;
  private final GeoLocator geoLocator;

  @Inject
  public TempTruckStopProvider(ObjectMapper mapper, GeoLocator geoLocator) {
    this.mapper = mapper;
    this.geoLocator = geoLocator;
  }

  @Override
  public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
    return type.equals(TempTruckStop.class);
  }

  @Override
  public TempTruckStop readFrom(Class<TempTruckStop> aClass, Type type, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> multivaluedMap,
      InputStream inputStream) throws IOException, WebApplicationException {
    TempTruckStop stop = mapper.readValue(inputStream, TempTruckStop.class);
    // verify that the location exists
    log.log(Level.INFO, "Updating temp stop {0}", stop.getLocationName());
    geoLocator.locateOpt(stop.getLocationName()).orElseThrow( () -> new WebApplicationException(400));
    return stop;
  }
}
