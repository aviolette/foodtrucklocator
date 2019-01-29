package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.net.UrlResource;
import foodtruck.schedule.SkeletonKeyReader;

/**
 * @author aviolette
 * @since 2018-12-19
 */
@Singleton
public class SeedSkeletonKeyServlet extends AbstractSeedServlet {

  @Inject
  public SeedSkeletonKeyServlet(TempTruckStopDAO tempDAO, SkeletonKeyReader reader, UrlResource urls) {
    super(tempDAO, reader, "https://www.skeletonkeybrewery.com/event-directory/", false, urls);
  }
}
