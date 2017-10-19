package foodtruck.util;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;

/**
 * @author aviolette
 * @since 7/20/16
 */
public class UrlBuilder {
  private final String uri;
  private final String queryString;
  private String host;
  private String protocol;
  private int port;


  public UrlBuilder(HttpServletRequest request) {
    protocol = request.getProtocol();
    host = request.getRemoteHost();
    port = request.getServerPort();
    uri = request.getRequestURI();
    queryString = request.getQueryString();
  }

  public UrlBuilder protocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  public UrlBuilder host(String host) {
    this.host = host;
    return this;
  }

  public UrlBuilder port(int port) {
    this.port = port;
    return this;
  }

  public String build() {
    StringBuilder builder = new StringBuilder();
    builder.append(protocol);
    builder.append("://");
    builder.append(host);
    if ("https".equals(protocol) && port != 443 || "http".equals(protocol) && port != 80) {
      builder.append(":").append(port);
    }
    builder.append(uri);
    if (!Strings.isNullOrEmpty(queryString)) {
      builder.append("?");
      builder.append(queryString);
    }
    return builder.toString();
  }
}
