package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.schedule.SquareSpaceEventReader;

/**
 * @author aviolette
 * @since 2019-01-06
 */
@Singleton
public class SeedTemperanceServlet extends AbstractSeedServlet {

  @Inject
  public SeedTemperanceServlet(TempTruckStopDAO tempDAO, Client client, SquareSpaceEventReader reader,
      @UserAgent String userAgent) {
    super(tempDAO, client, reader, "https://temperancebeer.com/events/", userAgent, false);
  }

  @Override
  protected String getCalendar() {
    return "temperance";
  }
}
