package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.schedule.PollyannaReader;

/**
 * @author aviolette
 * @since 2018-12-23
 */
@Singleton
public class SeedPollyannaServlet extends AbstractSeedServlet {

  @Inject
  public SeedPollyannaServlet(TempTruckStopDAO tempDAO, Client client, PollyannaReader reader,
      @UserAgent String userAgent) {
    super(tempDAO, client, reader,
        "https://inffuse.eventscalendar.co/js/v0.1/calendar/data?shop=pollyannabrewing.myshopify.com&inffuse-project=1&_referrer=",
        userAgent, false);
  }
}
