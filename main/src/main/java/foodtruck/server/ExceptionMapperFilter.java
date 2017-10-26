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
 * A filter that takes a {@link CodedServletException} and converts it to an HTTP response.
 *
 * @author aviolette
 * @since 10/26/17
 */
@Singleton
public class ExceptionMapperFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    try {
      filterChain.doFilter(request, response);
    } catch (CodedServletException ase) {
      ((HttpServletResponse)response).sendError(ase.getCode(), ase.getMessage());
    }
  }

  @Override
  public void destroy() {
  }
}
