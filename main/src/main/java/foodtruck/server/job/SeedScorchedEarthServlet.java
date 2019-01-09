package foodtruck.server.job;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.schedule.ScorchedEarthReader;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2019-01-01
 */
@Singleton
public class SeedScorchedEarthServlet extends AbstractJobServlet {

  private static final Logger log = Logger.getLogger(SeedScorchedEarthServlet.class.getName());

  private final TempTruckStopDAO tempDAO;
  private final Client client;
  private final ScorchedEarthReader reader;
  private final String userAgent;
  private final Clock clock;

  @Inject
  public SeedScorchedEarthServlet(TempTruckStopDAO tempDAO, Client client, ScorchedEarthReader reader,
      @UserAgent String userAgent, Clock clock) {
    this.tempDAO = tempDAO;
    this.client = client;
    this.reader = reader;
    this.userAgent = userAgent;
    this.clock = clock;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    ZonedDateTime time = clock.now8();
    int total = 0;
    for (int i=0; i < 3; i++) {
      String url = "http://scorchedearthbrewing.com/events/" + time.getYear() + "/" + pad(time.getMonthValue());
      log.log(Level.INFO, "Retrieving: {0}", url);
      String document = client.resource(url)
          .header(HttpHeaders.USER_AGENT, userAgent)
          .get(String.class);
      List<TempTruckStop> stops = reader.findStops(document);
      total += stops.size();
      stops.forEach(tempDAO::save);
      time = time.plusMonths(1);
    }
    log.log(Level.INFO, "Retrieved {0} stops for calendar: {1}", new Object[] {total, reader.getCalendar()});
  }

  private String pad(int monthValue) {
    if (monthValue < 10) {
      return "0" + monthValue;
    }
    return String.valueOf(monthValue);
  }
}
