package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.ThreeLeggedReader;

@Singleton
public class SeedThreeLeggedTacoScheduleServlet extends AbstractSeedServlet {

  @Inject
  public SeedThreeLeggedTacoScheduleServlet(TempTruckStopDAO tempDAO, ThreeLeggedReader reader, UrlResource urls,
      @Named("threeleggedtaco.url") Provider<String> urlProvider) {
    super(tempDAO, reader, false, urls, urlProvider);
  }
}
