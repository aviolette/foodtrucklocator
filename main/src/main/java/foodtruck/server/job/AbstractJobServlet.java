package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.utils.SystemProperty;

/**
 * @author aviolette
 * @since 2018-12-26
 */
public abstract class AbstractJobServlet extends HttpServlet {

  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (SystemProperty.environment.value() != SystemProperty.Environment.Value.Production) {
      doPost(req, resp);
    } else {
      super.doGet(req, resp);
    }
  }
}
