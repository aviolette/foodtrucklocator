package foodtruck.session;

import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.caching.Cacher;
import foodtruck.time.Clock;
import foodtruck.time.HttpHeaderFormat;

/**
 * A light-weight session I created since I only wanted sessions tracked in some of the lesser used-parts of my app
 * (not on the front-door).  This tracks sessions by storing a session cookie and storing data in memcached.
 *
 * @author aviolette
 * @since 6/18/14
 */
@SuppressWarnings("unused")
@RequestScoped
public class Session {
  private final Cacher cache;
  private final HttpServletResponse response;
  private String sessionKey;
  private DateTimeFormatter formatter;

  @Inject
  public Session(HttpServletRequest request, HttpServletResponse response, SessionIdentifier sessionIdentifier, Clock clock, @HttpHeaderFormat DateTimeFormatter formatter,
      Cacher cacher) {
    this.cache = cacher ;
    this.sessionKey = getSessionCookie(request);
    this.formatter = formatter;
    this.response = response;
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
      //noinspection unchecked
      contents = (Map<String, Object>) cache.get(fullSessionKey());
    } else {
      contents = Maps.newHashMap();
    }
    //noinspection ConstantConditions
    contents.put(name, value);
    cache.put(fullSessionKey(), contents, 1440);
  }

  public @Nullable Object getProperty(String name) {
    if (cache.contains(fullSessionKey())) {
      //noinspection unchecked
      Map<String, Object> map = (Map<String, Object>) cache.get(fullSessionKey());
      //noinspection ConstantConditions
      return map.get(name);
    }
    return null;
  }

  private String fullSessionKey() {
    return "session-" + sessionKey;
  }

  public void removeProperty(String name) {
    if (cache.contains(fullSessionKey())) {
      //noinspection unchecked
      Map<String, Object> contents = (Map<String, Object>) cache.get(fullSessionKey());
      //noinspection ConstantConditions
      contents.remove(name);
      cache.put(fullSessionKey(), contents);
    }
  }

  public void invalidate() {
    cache.delete(fullSessionKey());
    DateTime past= new DateTime(0);
    response.setHeader(HttpHeaders.SET_COOKIE, "session=deleted; Expires=" + formatter.print(past) +
        "; Path=/");

  }
}
