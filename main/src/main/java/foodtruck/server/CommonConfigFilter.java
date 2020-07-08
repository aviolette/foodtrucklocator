package foodtruck.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.annotations.LocalInstance;

/**
 * @author aviolette
 * @since 8/22/16
 */
@Singleton
public class CommonConfigFilter implements Filter {

  private final boolean localEnvironment;

  @Inject
  public CommonConfigFilter(@LocalInstance boolean localEnvironment) {
    this.localEnvironment = localEnvironment;
  }

  @Override
  public void init(FilterConfig filterConfig) {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    request.setAttribute("localFrameworks", localEnvironment);
    request.setAttribute("googleAnalytics", System.getProperty("foodtrucklocator.google.analytics", "UA-24765719-1"));
    filterChain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }
}

