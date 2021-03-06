package foodtruck.server.resources.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.TwitterNotificationAccount;

/**
 * @author aviolette
 * @since 12/4/12
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class TwitterNotificationAccountReader implements MessageBodyReader<TwitterNotificationAccount> {
  private final GeoLocator geoLocator;

  @Inject
  public TwitterNotificationAccountReader(GeoLocator geoLocator) {
    this.geoLocator = geoLocator;
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(TwitterNotificationAccount.class);
  }

  @Override
  public TwitterNotificationAccount readFrom(Class<TwitterNotificationAccount> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {
    try {
      JSONObject obj = new JSONObject(new String(ByteStreams.toByteArray(entityStream)));
      TwitterNotificationAccount.Builder builder = TwitterNotificationAccount.builder();

      builder.location(geoLocator.locate(obj.getString("location"), GeolocationGranularity.NARROW));
      return builder.oauthToken(obj.optString("token"))
          .oauthTokenSecret(obj.optString("tokenSecret"))
          .twitterHandle(obj.getString("twitterHandle"))
          .active(obj.optBoolean("active", true))
          .name(obj.getString("name"))
          .key(obj.optLong("id", 0))
          .build();
    } catch (JSONException e) {
      throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
}
