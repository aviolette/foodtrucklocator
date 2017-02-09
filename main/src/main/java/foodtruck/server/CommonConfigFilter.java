package foodtruck.server;

import java.io.IOException;

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
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    request.setAttribute("localFrameworks", "true".equals(System.getProperty("use.local.frameworks")));
    request.setAttribute("googleAnalytics", System.getProperty("foodtrucklocator.google.analytics", "UA-24765719-1"));
    filterChain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }
}

