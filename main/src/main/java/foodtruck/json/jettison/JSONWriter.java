package foodtruck.json.jettison;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
public interface JSONWriter<T> {
  JSONObject asJSON(T t) throws JSONException;
}

