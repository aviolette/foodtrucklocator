package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.util.Providers;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.PerknPickleReader;

@Singleton
public class SeedPerknPickleCalendar extends AbstractSeedServlet {

  @Inject
  public SeedPerknPickleCalendar(TempTruckStopDAO tempDAO, PerknPickleReader reader, UrlResource urls) {
    super(tempDAO, reader, false, urls, Providers.of("https://calendar.apps.secureserver.net/v1/events/4ea303ba-7e61-4ae2-b3eb-64ffe94b6959/8945879d-cee7-427e-9a35-ee9eb546a36c/619973fa-40f3-45ed-9f92-380ac1a03305"));
  }
}
