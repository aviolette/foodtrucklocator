package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.YourSistersReader;

@Singleton
public class SeedYourSistersScheduleServlet extends AbstractSeedServlet {

  @Inject
  public SeedYourSistersScheduleServlet(TempTruckStopDAO tempDAO, YourSistersReader reader, UrlResource urls) {
    super(tempDAO, reader, "https://www.yoursisterstomato.com/upcoming-events/", false, urls);
  }
}
