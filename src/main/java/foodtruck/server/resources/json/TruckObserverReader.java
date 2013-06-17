package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.TruckObserver;
import static foodtruck.server.resources.json.JSONSerializer.readJSON;

/**
 * @author aviolette
 * @since 6/11/13
 */
@Provider @Consumes(MediaType.APPLICATION_JSON)
public class TruckObserverReader implements MessageBodyReader<TruckObserver> {
  private final GeoLocator geolocator;

  @Inject
  public TruckObserverReader(GeoLocator locator) {
    this.geolocator = locator;
  }

  @Override public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(TruckObserver.class);
  }

  @Override public TruckObserver readFrom(Class<TruckObserver> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      JSONObject obj = readJSON(entityStream);
      Location loc = geolocator.locate(obj.getString("locationName"), GeolocationGranularity.BROAD);
      if (loc == null) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }
      List<String> keywords = ImmutableList.copyOf(Splitter.on(",")
          .omitEmptyStrings().trimResults().split(obj.getString("keywords")));
      return new TruckObserver(obj.getString("twitterHandle"), loc, keywords);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
