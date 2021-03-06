package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.util.Providers;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.CoastlineCalendarReader;

/**
 * @author aviolette
 * @since 2018-12-18
 */

@Singleton
public class SeedCoastlineScheduleServlet extends AbstractSeedServlet {

  @Inject
  public SeedCoastlineScheduleServlet(CoastlineCalendarReader reader, TempTruckStopDAO dao, UrlResource urls) {
    super(dao, reader, true, urls, Providers.of("https://cateredbycoastline.com"));
  }
}
