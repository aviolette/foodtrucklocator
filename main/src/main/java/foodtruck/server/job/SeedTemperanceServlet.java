package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.SquareSpaceEventReader;

/**
 * @author aviolette
 * @since 2019-01-06
 */
@Singleton
public class SeedTemperanceServlet extends AbstractSeedServlet {

  @Inject
  public SeedTemperanceServlet(TempTruckStopDAO tempDAO, SquareSpaceEventReader reader, UrlResource urls) {
    super(tempDAO, reader, "https://temperancebeer.com/events/", false, urls);
  }

  @Override
  protected String getCalendar() {
    return "temperance";
  }
}
