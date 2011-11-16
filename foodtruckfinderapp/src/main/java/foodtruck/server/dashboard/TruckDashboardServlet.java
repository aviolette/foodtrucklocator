package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.twitter.TwitterService;

/**
 * @author aviolette@gmail.com
 * @since 11/14/11
 */
@Singleton
public class TruckDashboardServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(TruckDashboardServlet.class.getName());
  private final TwitterService twitterService;

  @Inject
  public TruckDashboardServlet(TwitterService twitterService) {
    this.twitterService = twitterService;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String truckId = req.getRequestURI().substring(1);
    log.info("Loading dashboard for " + truckId);

    final String jsp = "/WEB-INF/jsp/dashboard/truckDashboard.jsp";
    // hack required when using * patterns in guice
    req = new HttpServletRequestWrapper(req) {
      public Object getAttribute(String name) {
        if ("org.apache.catalina.jsp_file".equals(name)) {
          return jsp;
        }
        return super.getAttribute(name);
      }
    };
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);
  }
}
