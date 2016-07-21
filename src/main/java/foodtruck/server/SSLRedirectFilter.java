package foodtruck.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.model.StaticConfig;
import foodtruck.util.UrlBuilder;

/**
 * @author aviolette
 * @since 7/19/16
 */
@Singleton
class SSLRedirectFilter implements Filter {
  private final StaticConfig config;

  @Inject
  public SSLRedirectFilter(StaticConfig config) {
    this.config = config;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(final ServletRequest req, final ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    if (!req.isSecure()) {
      HttpServletResponse resp = (HttpServletResponse) response;
      HttpServletRequest request = (HttpServletRequest) req;
      resp.sendRedirect(new UrlBuilder(request)
          .protocol("https")
          .port(443)
          .host(config.getBaseUrl())
          .build());
      return;
    }
    filterChain.doFilter(req, response);
  }

  @Override
  public void destroy() {
  }
}
