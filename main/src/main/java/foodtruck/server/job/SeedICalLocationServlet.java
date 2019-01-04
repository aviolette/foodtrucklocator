package foodtruck.server.job;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.schedule.LocationICalStopReader;

/**
 * @author aviolette
 * @since 2019-01-04
 */
@Singleton
public class SeedICalLocationServlet extends AbstractCalendarServlet {

  private final LocationICalStopReader reader;
  private final String userAgent;
  private final Client client;

  @Inject
  protected SeedICalLocationServlet(@UserAgent String userAgent, Client client, TempTruckStopDAO dao, LocationICalStopReader reader) {
    super(dao);
    this.reader = reader;
    this.userAgent = userAgent;
    this.client = client;
  }

  @Override
  protected List<TempTruckStop> doSearch(String calendar, String truck, String defaultLocation) {
    String document = client.resource(calendar)
        .header(HttpHeaders.USER_AGENT, userAgent)
        .get(String.class);
    return reader.findStops(document, defaultLocation, "iCal: " + defaultLocation);
  }
}
