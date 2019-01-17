package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.dao.RetweetsDAO;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.schedule.SocialMediaCacher;
import foodtruck.time.Clock;

/**
 * Reloads the external calendars into the internal schedule cache.
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
@Singleton
public class RecacheServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(RecacheServlet.class.getName());
  private final FoodTruckStopService service;
  private final Clock clock;
  private final SocialMediaCacher socialMediaCacher;
  private final RetweetsDAO retweetsDAO;

  @Inject
  public RecacheServlet(FoodTruckStopService service, Clock clock, SocialMediaCacher socialMediaCacher, RetweetsDAO retweetsDAO) {
    this.socialMediaCacher = socialMediaCacher;
    this.service = service;
    this.clock = clock;
    this.retweetsDAO = retweetsDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    LocalDate when = clock.currentDay();
    DateTimeZone zone = clock.zone();
    final Interval instant = when.toInterval(zone).withEnd(when.plusDays(365).toDateTimeAtStartOfDay(zone));
    log.info("Recaching all trucks");
    service.pullCustomCalendars(instant);
    socialMediaCacher.analyze();
    retweetsDAO.deleteAll();
  }
}
