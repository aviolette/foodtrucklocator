package tgc.server.resources;

import com.google.common.base.Throwables;
import com.javadocmd.simplelatlng.LatLng;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import tgc.model.Tweet;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author aviolette
 * @since 11/7/12
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class TweetsWriter implements MessageBodyWriter<List<Tweet>> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    // TODO: This will match way too many things, but for now, it works
    return true;
  }

  @Override
  public long getSize(List<Tweet> tweets, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(List<Tweet> tweets, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    JSONArray arr = new JSONArray();
    for (Tweet tweet : tweets) {
      JSONObject obj = new JSONObject();
      try {
        obj.put("id", tweet.getTweetId());
        obj.put("user", tweet.getScreenName());
        obj.put("text", tweet.getText());
        obj.put("time", tweet.getTime());
        LatLng loc = tweet.getLocation();
        if (loc != null) {
          JSONObject locObj = new JSONObject();
          locObj.put("lat", loc.getLatitude());
          locObj.put("lng", loc.getLongitude());
          obj.put("location", locObj);
        }
        obj.put("retweet", tweet.isRetweet());
        arr.put(obj);
      } catch (JSONException e) {
        throw Throwables.propagate(e);
      }
    }
    try {
      entityStream.write(arr.toString().getBytes("UTF-8"));
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
  }
}
