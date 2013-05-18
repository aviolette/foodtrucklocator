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

import foodtruck.dao.TruckDAO;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.twitter.TwitterService;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
@Singleton
public class RecacheServlet extends HttpServlet {
  private final FoodTruckStopService service;
  private final Clock clock;
  private final TwitterService twitterService;
  private final TruckDAO truckDAO;
  private final DateTimeFormatter timeFormatter;
  private static final Logger log = Logger.getLogger(RecacheServlet.class.getName());
  private final DateTimeZone zone;

  @Inject
  public RecacheServlet(FoodTruckStopService service, Clock clock,
      TwitterService twitterService, TruckDAO truckDAO, DateTimeZone zone) {
    this.twitterService = twitterService;
    this.service = service;
    this.clock = clock;
    this.truckDAO = truckDAO;
    this.timeFormatter = DateTimeFormat.forPattern("YYYYMMdd").withZone(zone);
    this.zone = zone;

  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String truck = req.getParameter("truck");
    final String date = req.getParameter("date");

    LocalDate when = parseDate(date);
    final Interval instant = when.toInterval(zone).withEnd(when.plusDays(7).toDateMidnight());
    if (!Strings.isNullOrEmpty(truck)) {
      service.updateStopsForTruck(instant, truckDAO.findById(truck));
    } else {
      service.updateStopsFor(instant);
    }
    twitterService.twittalyze();
    twitterService.observerTwittalyze();
  }

  private LocalDate parseDate(String date) {
    if (!Strings.isNullOrEmpty(date)) {
      try {
        return timeFormatter.parseDateTime(date).toDateMidnight().toLocalDate();
      } catch (Exception e) {
        log.log(Level.WARNING, "Error formatting time", e);
        Throwables.propagate(e);
      }
    }
    return clock.currentDay();
  }
}
