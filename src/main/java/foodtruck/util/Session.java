package foodtruck.util;

import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

/**
 * A light-weight session I created since I only wanted sessions tracked in some of the lesser used-parts of my app
 * (not on the front-door).  This tracks sessions by storing a session cookie and storing data in memcached.
 *
 * @author aviolette
 * @since 6/18/14
 */
@RequestScoped
public class Session {
  private final MemcacheService cache;
  private String sessionKey;

  @Inject
  public Session(HttpServletRequest request, HttpServletResponse response, MemcacheService cache,
      SessionIdentifier sessionIdentifier, Clock clock, @HttpHeaderFormat DateTimeFormatter formatter) {
    this.cache = cache;
    this.sessionKey = getSessionCookie(request);
    if (sessionKey == null) {
      sessionKey = sessionIdentifier.nextSessionId();
      DateTime tomorrow = clock.now().plusDays(1);
      response.setHeader(HttpHeaders.SET_COOKIE, "session=" + sessionKey + "; Expires=" + formatter.print(tomorrow) +
          "; Path=/");
    }
  }

  private @Nullable String getSessionCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if ("session".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

  public void setProperty(String name, Object value) {
    Map<String, Object> contents;
    if (cache.contains(fullSessionKey())) {
      contents = (Map<String, Object>) cache.get(fullSessionKey());
    } else {
      contents = Maps.newHashMap();
    }
    contents.put(name, value);
    cache.put(fullSessionKey(), contents, Expiration.byDeltaSeconds(86400));
  }

  public @Nullable Object getProperty(String name) {
    if (cache.contains(fullSessionKey())) {
      Map<String, Object> map = (Map<String, Object>) cache.get(fullSessionKey());
      return map.get(name);
    }
    return null;
  }

  public String fullSessionKey() {
    return "session-" + sessionKey;
  }

  public void removeProperty(String name) {
    if (cache.contains(fullSessionKey())) {
      Map<String, Object> contents = (Map<String, Object>) cache.get(fullSessionKey());
      contents.remove(name);
      cache.put(fullSessionKey(), contents);
    }
  }
}
