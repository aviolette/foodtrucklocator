package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.schedule.WerkforceReader;

/**
 * @author aviolette
 * @since 2019-01-01
 */
@Singleton
public class SeedWerkforceServlet extends AbstractSeedServlet {

  @Inject
  public SeedWerkforceServlet(TempTruckStopDAO tempDAO, Client client, WerkforceReader reader,
      @UserAgent String userAgent) {
    super(tempDAO, client, reader, "http://www.werkforcebrewing.com/events/", userAgent, false);
  }
}
