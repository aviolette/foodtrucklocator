package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.twitter.TwitterService;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
@Singleton
public class FoodTruckUpdaterServlet extends HttpServlet implements Runnable {
  private final FoodTruckStopService service;
  private final Clock clock;
  private final TwitterService twitterService;

  @Inject
  public FoodTruckUpdaterServlet(FoodTruckStopService service, Clock clock, TwitterService twitterService) {
    this.twitterService = twitterService;
    this.service = service;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    run();
  }

  @Override
  public void run() {
    service.updateStopsFor(clock.currentDay());
    twitterService.updateLocationsOfTwitterTrucks();
  }
}
