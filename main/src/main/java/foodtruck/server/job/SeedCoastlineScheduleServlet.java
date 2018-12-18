package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.schedule.CoastlineCalendarReader;

/**
 * @author aviolette
 * @since 2018-12-18
 */

@Singleton
public class SeedCoastlineScheduleServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(SeedCoastlineScheduleServlet.class.getName());

  private final Client client;
  private final String userAgent;
  private final CoastlineCalendarReader reader;
  private final TempTruckStopDAO dao;

  @Inject
  public SeedCoastlineScheduleServlet(Client client, @UserAgent String userAgent, CoastlineCalendarReader reader, TempTruckStopDAO dao) {
    this.client = client;
    this.userAgent = userAgent;
    this.dao = dao;
    this.reader = reader;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String document = client.resource("https://cateredbycoastline.com")
        .header(HttpHeaders.USER_AGENT, userAgent)
        .get(String.class);
    for (TempTruckStop stop : reader.findStops(document)) {
      dao.save(stop);
    }
    resp.setStatus(200);
  }
}
