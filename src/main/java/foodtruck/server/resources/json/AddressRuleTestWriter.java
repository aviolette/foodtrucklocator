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

import foodtruck.model.AddressRuleTest;

/**
 * @author aviolette@gmail.com
 * @since 9/4/12
 */
@Provider @Produces(MediaType.APPLICATION_JSON)
public class AddressRuleTestWriter implements JSONWriter<AddressRuleTest>, MessageBodyWriter<AddressRuleTest> {
  @Override public JSONObject asJSON(AddressRuleTest addressRule) throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("name", addressRule.getName());
    jsonObject.put("input", addressRule.getInput());
    jsonObject.put("expected", addressRule.getExpected());
    jsonObject.put("truck", addressRule.getTruck());
    if (!addressRule.isNew()) {
      jsonObject.put("id", addressRule.getKey());
    }
    return jsonObject;
  }

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type.equals(AddressRuleTest.class);
  }

  @Override public long getSize(AddressRuleTest addressRule, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override public void writeTo(AddressRuleTest addressRule, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(addressRule), entityStream);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
