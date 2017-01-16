package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author aviolette
 * @since 1/12/17
 */

@Singleton
public class CheckDeviceIssuesServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(CheckDeviceIssuesServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    log.log(Level.INFO, "Checking devices");
    // TODO: put stuff here
  }
}
