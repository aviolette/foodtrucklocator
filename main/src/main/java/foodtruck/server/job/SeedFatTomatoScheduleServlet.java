package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.util.Providers;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.FatTomatoReader;

/**
 * @author aviolette
 * @since 2018-12-24
 */
@Singleton
public class SeedFatTomatoScheduleServlet extends AbstractSeedServlet {

  @Inject
  public SeedFatTomatoScheduleServlet(TempTruckStopDAO tempDAO, FatTomatoReader reader, UrlResource urls) {
    super(tempDAO, reader, false, urls, Providers.of(
        "https://calendar.apps.secureserver.net/v1/events/c970aa80-a89d-4fb4-a22a-a4b37fcef0f9/f63d5c95-e7cc-45bf-a559-6305a5ccc035/260e566b-2e02-41de-b794-f3597d6f4aef"));
  }
}
