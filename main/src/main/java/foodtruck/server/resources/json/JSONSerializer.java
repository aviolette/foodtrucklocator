package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
public class JSONSerializer {
  public static boolean isParameterizedCollectionOf(Type type, Class<?> aClass,
      Class<?> contained) {
    if (!(type instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType pt = (ParameterizedType) type;
    return Iterable.class.isAssignableFrom(aClass) &&
        pt.getActualTypeArguments().length == 1 &&
        pt.getActualTypeArguments()[0].equals(contained);
  }

  public static <E> void writeJSONCollection(Iterable<E> objs, JSONWriter<E> writer,
      OutputStream os) throws JSONException {
    JSONArray arr = buildArray(objs, writer);
    writeJSON(arr, os);
  }

  public static void writeJSON(Object obj, OutputStream os) {
    try {
      os.write(obj.toString().getBytes("UTF-8"));
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
  }

  public static JSONObject readJSON(InputStream entityStream) throws JSONException {
    try {
      String input = new String(ByteStreams.toByteArray(entityStream));
      return new JSONObject(input);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <E> JSONArray buildArray(Iterable<E> objs, JSONWriter<E> writer)
      throws JSONException {
    JSONArray arr = new JSONArray();
    for (E t : objs) {
      arr.put(writer.asJSON(t));
    }
    return arr;
  }

  public static List<String> toStringList(JSONArray truckIds) throws JSONException {
    ImmutableList.Builder<String> list = ImmutableList.builder();
    for (int i=0; i < truckIds.length(); i++) {
      list.add(truckIds.getString(i));
    }
    return list.build();
  }
}