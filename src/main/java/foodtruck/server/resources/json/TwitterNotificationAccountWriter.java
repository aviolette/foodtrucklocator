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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.TwitterNotificationAccount;
import foodtruck.server.resources.BadRequestException;

/**
 * @author aviolette
 * @since 12/5/12
 */
@Provider @Produces(MediaType.APPLICATION_JSON)
public class TwitterNotificationAccountWriter implements MessageBodyWriter<TwitterNotificationAccount>,
    JSONWriter<TwitterNotificationAccount> {
  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(TwitterNotificationAccount.class);
  }

  @Override public long getSize(TwitterNotificationAccount twitterNotificationAccount, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override public void writeTo(TwitterNotificationAccount twitterNotificationAccount, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSON(asJSON(twitterNotificationAccount), entityStream);
    } catch (JSONException e) {
      throw new BadRequestException(e, MediaType.APPLICATION_JSON_TYPE);
    }

  }

  public JSONObject asJSON(TwitterNotificationAccount notificationAccount) throws JSONException {
    return new JSONObject()
        .put("name", notificationAccount.getName())
        .put("location", notificationAccount.getLocation().getName())
        .put("token", notificationAccount.getOauthToken())
        .put("tokenSecret", notificationAccount.getOauthTokenSecret())
        .put("twitterHandle", notificationAccount.getTwitterHandle());
  }
}
