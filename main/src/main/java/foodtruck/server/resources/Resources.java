package foodtruck.server.resources;

import com.sun.jersey.api.JResponse;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
class Resources {
  static <T> JResponse.JResponseBuilder<T> noCache(JResponse.JResponseBuilder<T> jResponse) {
    return jResponse.header("Cache-Control", "no-cache")
        .header("Pragma", "no-cache")
        .header("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
  }
}