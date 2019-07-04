package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.util.Providers;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.YourSistersReader;

@Singleton
public class SeedYourSistersScheduleServlet extends AbstractSeedServlet {

  @Inject
  public SeedYourSistersScheduleServlet(TempTruckStopDAO tempDAO, YourSistersReader reader, UrlResource urls) {
    super(tempDAO, reader, false, urls, Providers.of("https://www.yoursisterstomato.com/upcoming-events/"));
  }
}
