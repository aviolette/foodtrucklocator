package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.truckstops.ScheduleCacher;

/**
 * Called to invalidate the schedule cache in memcached.
 * @author aviolette
 * @since 5/29/13
 */
@Singleton
public class InvalidateScheduleCache extends HttpServlet {
  private static final Logger log = Logger.getLogger(InvalidateScheduleCache.class.getName());
  private final ScheduleCacher cacher;

  @Inject
  public InvalidateScheduleCache(ScheduleCacher cacher) {
    this.cacher = cacher;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    log.info("Forcing cache invalidation");
    cacher.invalidate();
  }
}
