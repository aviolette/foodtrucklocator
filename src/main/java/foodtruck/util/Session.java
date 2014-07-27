package foodtruck.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

/**
 * A light-weight session I created since I only wanted sessions tracked in some of the lesser used-parts of my app
 * (not on the front-door).  This tracks sessions by storing a session cookie and storing data in memcached.
 *
 * @author aviolette
 * @since 6/18/14
 */
@RequestScoped
public class Session {
  private final HttpServletResponse response;
  private final HttpServletRequest request;
  private final MemcacheService cache;
  private final SessionIdentifier sessionIdentifier;

  @Inject
  public Session(HttpServletRequest request, HttpServletResponse response, MemcacheService cache, SessionIdentifier sessionIdentifier) {
    this.request = request;
    this.response = response;
    this.cache = cache;
    this.sessionIdentifier = sessionIdentifier;
  }

  public void setProperty(String name, Object value) {
  }
}
