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

import foodtruck.model.AddressRule;
import foodtruck.server.resources.BadRequestException;
import static foodtruck.server.resources.json.JSONSerializer.readJSON;

/**
 * @author aviolette@gmail.com
 * @since 8/20/12
 */
@Provider @Consumes(MediaType.APPLICATION_JSON)
public class AddressRuleReader implements MessageBodyReader<AddressRule> {
  @Override public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type.equals(AddressRule.class);
  }

  @Override
  public AddressRule readFrom(Class<AddressRule> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    try {
      JSONObject json = readJSON(entityStream);
      return AddressRule.builder()
          .pattern(json.getString("pattern"))
          .build();
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }
  }
}
