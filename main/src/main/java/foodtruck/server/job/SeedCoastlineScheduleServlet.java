package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.schedule.CoastlineCalendarReader;

/**
 * @author aviolette
 * @since 2018-12-18
 */

@Singleton
public class SeedCoastlineScheduleServlet extends AbstractSeedServlet {

  @Inject
  public SeedCoastlineScheduleServlet(Client client, @UserAgent String userAgent, CoastlineCalendarReader reader,
      TempTruckStopDAO dao) {
    super(dao, client, reader, "https://cateredbycoastline.com", userAgent, true);
  }
}
