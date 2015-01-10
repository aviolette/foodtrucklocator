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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.beaconnaise.BeaconSignal;
import foodtruck.model.Location;
import foodtruck.server.resources.BadRequestException;

import static foodtruck.server.resources.json.JSONSerializer.readJSON;

/**
 * @author aviolette
 * @since 7/24/13
 */
@Provider @Consumes(MediaType.APPLICATION_JSON)
public class BeaconSignalReader  implements MessageBodyReader<BeaconSignal> {

  @Override public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(BeaconSignal.class);
  }

  @Override public BeaconSignal readFrom(Class<BeaconSignal> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      JSONObject json = readJSON(entityStream);
      JSONObject location = json.getJSONObject("location");
      return new BeaconSignal(json.getString("truckId"),
          Location.builder().lat(location.getDouble("latitude")).lng(location.getDouble("longitude")).build());
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }
}
