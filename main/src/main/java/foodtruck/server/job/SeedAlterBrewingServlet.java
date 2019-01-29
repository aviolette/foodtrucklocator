package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.AlterBrewingReader;

/**
 * @author aviolette
 * @since 2018-12-28
 */
@Singleton
public class SeedAlterBrewingServlet extends AbstractSeedServlet {

  @Inject
  public SeedAlterBrewingServlet(TempTruckStopDAO tempDAO, AlterBrewingReader reader, UrlResource urls) {
    super(tempDAO, reader, "https://www.alterbrewing.com/events/", false, urls);
  }
}
