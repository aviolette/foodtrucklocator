package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.util.Providers;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.FatShallotScheduleReader;

/**
 * @author aviolette
 * @since 2018-12-24
 */
@Singleton
public class SeedFatShallotSchedule extends AbstractSeedServlet {

  @Inject
  public SeedFatShallotSchedule(TempTruckStopDAO tempDAO, FatShallotScheduleReader reader, UrlResource urls) {
    super(tempDAO, reader, false, urls, Providers.of("http://thefatshallot.com/schedule/"));
  }
}
