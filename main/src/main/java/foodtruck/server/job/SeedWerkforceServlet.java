package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.util.Providers;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.WerkforceReader;

/**
 * @author aviolette
 * @since 2019-01-01
 */
@Singleton
public class SeedWerkforceServlet extends AbstractSeedServlet {

  @Inject
  public SeedWerkforceServlet(TempTruckStopDAO tempDAO, WerkforceReader reader, UrlResource urls) {
    super(tempDAO, reader, false, urls, Providers.of("https://www.werkforcebrewing.com/events/"));
  }
}
