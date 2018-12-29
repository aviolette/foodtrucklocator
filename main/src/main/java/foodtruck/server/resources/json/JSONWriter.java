package foodtruck.server.resources.json;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
public interface JSONWriter<T> {

  Logger log = Logger.getLogger(JSONWriter.class.getName());

  JSONObject asJSON(T t) throws JSONException;

  default String asString(T t) {
    return tryAsJson(t).toString();
  }

  default JSONObject tryAsJson(T t) {
    try {
      return asJSON(t);
    } catch (JSONException e) {
      log.log(Level.WARNING, e.getMessage(), e);
      throw new RuntimeException(e);
    }
  };
}

