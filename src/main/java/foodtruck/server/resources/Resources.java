package foodtruck.server.resources;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.sun.jersey.api.JResponse;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
public class Resources {
  public static <T> JResponse.JResponseBuilder<T> noCache(JResponse.JResponseBuilder<T> jResponse) {
    return jResponse.header("Cache-Control", "no-cache")
        .header("Pragma", "no-cache")
        .header("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
  }

  public static void requiresAdmin() {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn() || !userService.isUserAdmin()) {
      Response response = Response.status(Response.Status.FORBIDDEN)
          .type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorPayload("fobidden"))
          .build();
      throw new WebApplicationException(response);
    }
  }
}