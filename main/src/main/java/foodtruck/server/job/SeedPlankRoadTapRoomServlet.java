package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.util.Providers;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.SquareSpaceEventReader;

/**
 * @author aviolette
 * @since 2018-12-27
 */
@Singleton
public class SeedPlankRoadTapRoomServlet extends AbstractSeedServlet {

  @Inject
  public SeedPlankRoadTapRoomServlet(TempTruckStopDAO tempDAO, SquareSpaceEventReader reader, UrlResource urls) {
    super(tempDAO, reader, false, urls, Providers.of("http://www.plankroadtaproom.com/events/"));
  }

  @Override
  protected String getCalendar() {
    return "plankroadtaproom";
  }
}
