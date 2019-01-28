package foodtruck.server.job;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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
import foodtruck.schedule.ICalStopReader;
import foodtruck.schedule.SquarespaceLinkExtractor;

/**
 * @author aviolette
 * @since 2019-01-28
 */
@Singleton
public class SeedBigWangsServlet extends AbstractJobServlet {

  private static final String BASE_URL = "https://www.bigwangsfoodtruck.com/events";
  private final Client client;
  private final TempTruckStopDAO tempDAO;
  private final String userAgent;
  private final SquarespaceLinkExtractor extractor;
  private final ICalStopReader reader;

  @Inject
  public SeedBigWangsServlet(TempTruckStopDAO tempDAO, Client client, @UserAgent String userAgent,
      SquarespaceLinkExtractor extractor, ICalStopReader reader) {
    this.client = client;
    this.tempDAO = tempDAO;
    this.userAgent = userAgent;
    this.extractor = extractor;
    this.reader = reader;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    List<TempTruckStop> stops = new LinkedList<>();
    for (String link : extractor.findLinks(client.resource("https://www.bigwangsfoodtruck.com/events")
        .header(HttpHeaders.USER_AGENT, userAgent)
        .get(String.class), BASE_URL)) {
      String iCalDocument = client.resource(link)
          .header(HttpHeaders.USER_AGENT, userAgent)
          .get(String.class);
      stops.addAll(reader.findStops(iCalDocument, "bigwangschicago"));
    }
    stops.forEach(tempDAO::save);
  }
}
