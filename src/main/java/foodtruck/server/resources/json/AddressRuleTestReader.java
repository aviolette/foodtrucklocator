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

import foodtruck.model.AddressRuleTest;
import foodtruck.server.resources.BadRequestException;
import static foodtruck.server.resources.json.JSONSerializer.readJSON;

/**
 * @author aviolette@gmail.com
 * @since 9/4/12
 */
@Provider @Consumes(MediaType.APPLICATION_JSON)
public class AddressRuleTestReader implements MessageBodyReader<AddressRuleTest> {
  @Override public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type.equals(AddressRuleTest.class);
  }

  @Override
  public AddressRuleTest readFrom(Class<AddressRuleTest> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      JSONObject json = readJSON(entityStream);
      return AddressRuleTest.builder()
          .name(json.getString("name"))
          .expected(json.getString("expected"))
          .truck(json.getString("truckId"))
          .input(json.getString("input"))
          .build();
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }
}
