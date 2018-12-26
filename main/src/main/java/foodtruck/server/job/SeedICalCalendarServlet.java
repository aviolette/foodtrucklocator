package foodtruck.server.job;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.schedule.ICalStopReader;

/**
 * @author aviolette
 * @since 2018-12-26
 */
@Singleton
public class SeedICalCalendarServlet extends AbstractJobServlet {

  private static final Logger log = Logger.getLogger(SeedICalCalendarServlet.class.getName());

  private final String userAgent;
  private final TempTruckStopDAO tempDAO;
  private final Client client;
  private final ICalStopReader stopReader;

  @Inject
  public SeedICalCalendarServlet(@UserAgent String userAgent, TempTruckStopDAO tempDAO, Client client,
      ICalStopReader stopReader) {
    this.userAgent = userAgent;
    this.tempDAO = tempDAO;
    this.client = client;
    this.stopReader = stopReader;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String calendar = req.getParameter("calendar");
    String truck = req.getParameter("truck");
    if (Strings.isNullOrEmpty(calendar)) {
      log.severe("Calendar is empty");
      return;
    }
    if (Strings.isNullOrEmpty(truck)) {
      log.severe("Truck is empty");
      return;
    }
    log.log(Level.INFO, "Searching calendar {0} for truck {1}", new Object[] {calendar, truck});
    String document = client.resource(calendar)
        .header(HttpHeaders.USER_AGENT, userAgent)
        .get(String.class);
    List<TempTruckStop> stops = stopReader.findStops(document, truck);
    log.log(Level.INFO, "Retrieved {0} stops for truck {2} with calendar: {1}", new Object[] {stops.size(), calendar, truck});
    stops.forEach(tempDAO::save);
    resp.setStatus(200);
  }
}
