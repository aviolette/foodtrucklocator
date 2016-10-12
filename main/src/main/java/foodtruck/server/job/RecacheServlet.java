package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.RetweetsDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.StaticConfig;
import foodtruck.socialmedia.SocialMediaCacher;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

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
  private final TruckDAO truckDAO;
  private final DateTimeFormatter timeFormatter;
  private final DateTimeZone zone;
  private final RetweetsDAO retweetsDAO;
  private final StaticConfig staticConfig;

  @Inject
  public RecacheServlet(FoodTruckStopService service, Clock clock, StaticConfig staticConfig,
      SocialMediaCacher socialMediaCacher, TruckDAO truckDAO, DateTimeZone zone, RetweetsDAO retweetsDAO) {
    this.socialMediaCacher = socialMediaCacher;
    this.service = service;
    this.clock = clock;
    this.truckDAO = truckDAO;
    this.timeFormatter = DateTimeFormat.forPattern("YYYYMMdd").withZone(zone);
    this.zone = zone;
    this.retweetsDAO = retweetsDAO;
    this.staticConfig = staticConfig;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String truck = req.getParameter("truck");
    final String date = req.getParameter("date");

    LocalDate when = parseDate(date);
    final Interval instant = when.toInterval(zone).withEnd(when.plusDays(7).toDateTimeAtStartOfDay(zone));
    if (!Strings.isNullOrEmpty(truck)) {
      log.info("Recaching truck: " + truck);
      service.pullCustomCalendarFor(instant, truckDAO.findById(truck));
    } else {
      if (staticConfig.isRecachingEnabled()) {
        log.info("Recaching all trucks");
        service.pullCustomCalendars(instant);
      } else {
        log.info("Recaching disabled");
      }
    }
    socialMediaCacher.analyze();
    retweetsDAO.deleteAll();
  }

  private LocalDate parseDate(String date) {
    if (!Strings.isNullOrEmpty(date)) {
      try {
        return timeFormatter.parseDateTime(date).withTimeAtStartOfDay().toLocalDate();
      } catch (Exception e) {
        log.log(Level.WARNING, "Error formatting time", e);
        Throwables.propagate(e);
      }
    }
    return clock.currentDay();
  }
}
