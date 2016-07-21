package foodtruck.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.MoreObjects;
import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 7/21/16
 */
@Singleton
class SiteScraperFilter implements Filter {
  private static final Logger log = Logger.getLogger(SiteScraperFilter.class.getName());

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    log.info("Initializing site scraper filter");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    String userAgent = MoreObjects.firstNonNull(((HttpServletRequest)request).getHeader("User-Agent"), "");
    if (userAgent.contains("domainreanimator")) {
      ((HttpServletResponse)response).sendError(404);
      return;
    }
    filterChain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }
}
