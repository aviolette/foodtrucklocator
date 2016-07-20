package foodtruck.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 7/19/16
 */
@Singleton
class SSLRedirectFilter implements Filter {
  private String redirectTo;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    redirectTo = filterConfig.getInitParameter("redirectTo");
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    if (!req.isSecure()) {
      HttpServletResponse resp = (HttpServletResponse) response;
      resp.sendRedirect(redirectTo);
      return;
    }
    filterChain.doFilter(req, response);
  }

  @Override
  public void destroy() {
  }
}
