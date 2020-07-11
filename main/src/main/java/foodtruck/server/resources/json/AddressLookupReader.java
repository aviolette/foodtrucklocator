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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.AddressLookup;
import foodtruck.server.resources.BadRequestException;

import static foodtruck.server.resources.json.JSONSerializer.readJSON;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class AddressLookupReader implements MessageBodyReader<AddressLookup> {

  private final TruckDAO truckDAO;

  @Inject
  public AddressLookupReader(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @Override
  public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
    return type.equals(AddressLookup.class);
  }

  @Override
  public AddressLookup readFrom(Class<AddressLookup> aClass, Type type, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> multivaluedMap,
      InputStream inputStream) throws IOException, WebApplicationException {
    try {
      JSONObject json = readJSON(inputStream);
      String truckId = json.getString("truckId");
      return new AddressLookup(json.getString("text"), truckDAO.findByIdOpt(truckId).orElseThrow(() -> new WebApplicationException(400)));
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }
}
