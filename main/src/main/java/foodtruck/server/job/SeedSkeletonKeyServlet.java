package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.schedule.SkeletonKeyReader;

/**
 * @author aviolette
 * @since 2018-12-19
 */
@Singleton
public class SeedSkeletonKeyServlet extends AbstractSeedServlet {

  @Inject
  public SeedSkeletonKeyServlet(TempTruckStopDAO tempDAO, Client client, @UserAgent String userAgent,
      SkeletonKeyReader reader) {
    super(tempDAO, client, reader, "https://www.skeletonkeybrewery.com/event-directory/", userAgent, false);
  }
}
