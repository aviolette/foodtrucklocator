package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.schedule.FatShallotScheduleReader;

/**
 * @author aviolette
 * @since 2018-12-24
 */
@Singleton
public class SeedFatShallotSchedule extends AbstractSeedServlet {

  @Inject
  public SeedFatShallotSchedule(TempTruckStopDAO tempDAO, Client client, FatShallotScheduleReader reader,
      @UserAgent String userAgent) {
    super(tempDAO, client, reader, "http://thefatshallot.com/schedule/", userAgent);
  }
}
