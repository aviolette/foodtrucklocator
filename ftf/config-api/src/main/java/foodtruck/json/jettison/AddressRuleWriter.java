package foodtruck.json.jettison;

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

import foodtruck.model.AddressRuleScript;

/**
 * @author aviolette@gmail.com
 * @since 8/20/12
 */
@Provider @Produces(MediaType.APPLICATION_JSON)
public class AddressRuleWriter implements JSONWriter<AddressRuleScript>, MessageBodyWriter<AddressRuleScript> {
  @Override public JSONObject asJSON(AddressRuleScript AddressRuleScript) throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("script", AddressRuleScript.getScript());
    return jsonObject;
  }

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type.equals(AddressRuleScript.class);
  }

  @Override public long getSize(AddressRuleScript AddressRuleScript, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override public void writeTo(AddressRuleScript AddressRuleScript, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(AddressRuleScript), entityStream);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
