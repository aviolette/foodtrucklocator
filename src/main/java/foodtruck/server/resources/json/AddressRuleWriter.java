package foodtruck.server.resources.json;

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

import com.google.common.base.Throwables;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.AddressRule;

/**
 * @author aviolette@gmail.com
 * @since 8/20/12
 */
@Provider @Produces(MediaType.APPLICATION_JSON)
public class AddressRuleWriter implements JSONWriter<AddressRule>, MessageBodyWriter<AddressRule> {
  @Override public JSONObject asJSON(AddressRule addressRule) throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("pattern", addressRule.getPattern());
    return jsonObject;
  }

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type.equals(AddressRule.class);
  }

  @Override public long getSize(AddressRule addressRule, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override public void writeTo(AddressRule addressRule, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(addressRule), entityStream);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
