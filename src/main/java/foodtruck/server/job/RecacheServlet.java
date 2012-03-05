package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.twitter.TwitterService;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
@Singleton
public class RecacheServlet extends HttpServlet {
  private final FoodTruckStopService service;
  private final Clock clock;
  private final TwitterService twitterService;
  private final TruckDAO truckDAO;

  @Inject
  public RecacheServlet(FoodTruckStopService service, Clock clock,
      TwitterService twitterService, TruckDAO truckDAO) {
    this.twitterService = twitterService;
    this.service = service;
    this.clock = clock;
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String truck = req.getParameter("truck");
    if (!Strings.isNullOrEmpty(truck)) {
      service.updateStopsForTruck(clock.currentDay(), truckDAO.findById(truck));
    } else {
      service.updateStopsFor(clock.currentDay());
    }
    twitterService.twittalyze();
  }
}
