package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.schedule.PlankRoadTapRoomReader;

/**
 * @author aviolette
 * @since 2018-12-27
 */
@Singleton
public class SeedPlankRoadTapRoomServlet extends AbstractSeedServlet {

  @Inject
  public SeedPlankRoadTapRoomServlet(TempTruckStopDAO tempDAO, Client client,
      PlankRoadTapRoomReader reader, @UserAgent String userAgent) {
    super(tempDAO, client, reader, "http://www.plankroadtaproom.com/events/", userAgent, false);
  }
}
