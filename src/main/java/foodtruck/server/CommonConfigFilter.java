package foodtruck.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 8/22/16
 */
@Singleton
public class CommonConfigFilter implements Filter {
  private static final Logger log = Logger.getLogger(SiteScraperFilter.class.getName());

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    request.setAttribute("localFrameworks", "true".equals(System.getProperty("use.local.frameworks")));
    filterChain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }
}

