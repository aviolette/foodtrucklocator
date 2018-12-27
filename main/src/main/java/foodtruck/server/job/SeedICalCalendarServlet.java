package foodtruck.server.job;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;

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
public class SeedICalCalendarServlet extends AbstractCalendarServlet {
  private final String userAgent;
  private final Client client;
  private final ICalStopReader stopReader;

  @Inject
  public SeedICalCalendarServlet(@UserAgent String userAgent, TempTruckStopDAO tempDAO, Client client,
      ICalStopReader stopReader) {
    super(tempDAO);
    this.userAgent = userAgent;
    this.client = client;
    this.stopReader = stopReader;
  }

  @Override
  protected List<TempTruckStop> doSearch(String calendar, String truck, String defaultLocation) {
    String document = client.resource(calendar)
        .header(HttpHeaders.USER_AGENT, userAgent)
        .get(String.class);
    return stopReader.findStops(document, truck);
  }
}
