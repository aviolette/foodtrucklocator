package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.schedule.AlterBrewingReader;

/**
 * @author aviolette
 * @since 2018-12-28
 */
@Singleton
public class SeedAlterBrewingServlet extends AbstractSeedServlet {

  @Inject
  public SeedAlterBrewingServlet(TempTruckStopDAO tempDAO, Client client, AlterBrewingReader reader,
      @UserAgent String userAgent) {
    super(tempDAO, client, reader, "https://www.alterbrewing.com/events/", userAgent, false);
  }
}
