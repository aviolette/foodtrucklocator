package foodtruck.server.dashboard;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Trucks;
import foodtruck.model.TweetSummary;
import foodtruck.server.api.JsonWriter;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.twitter.TwitterService;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since 11/14/11
 */
@Singleton
public class TruckServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(TruckServlet.class.getName());
  private final TwitterService twitterService;
  private final Trucks trucks;
  private final FoodTruckStopService truckService;
  private final Clock clock;
  private final JsonWriter writer;

  @Inject
  public TruckServlet(TwitterService twitterService, Trucks trucks,
      FoodTruckStopService truckService, Clock clock, JsonWriter writer) {
    this.twitterService = twitterService;
    this.trucks = trucks;
    this.truckService = truckService;
    this.clock = clock;
    this.writer = writer;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    final String truckId = requestURI.substring(14);
    log.info("Loading dashboard for " + truckId);
    final List<TweetSummary> tweetSummaries = twitterService.findForTruck(truckId);
    req.setAttribute("tweets", tweetSummaries);
    final String jsp = "/WEB-INF/jsp/dashboard/truckDashboard.jsp";
    // hack required when using * patterns in guice
    req = new HttpServletRequestWrapper(req) {
      public Object getAttribute(String name) {
        if ("org.apache.catalina.jsp_file".equals(name)) {
          return jsp;
        }
        return super.getAttribute(name);
      }
    };
    req.setAttribute("headerName", trucks.findById(truckId).getName());
    try {
      req.setAttribute("schedule", writer.writeSchedule(truckService.findStopsForDay(truckId, clock.currentDay())));
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String body = new String(ByteStreams.toByteArray(req.getInputStream()));
    body = URLDecoder.decode(body, "UTF-8");
    try {
      JSONObject bodyObj = new JSONObject(body);
      final long tweetId = Long.parseLong(bodyObj.getString("id"));
      TweetSummary summary = twitterService.findByTweetId(tweetId);
      if (summary == null) {
        log.warning("COULDN'T FIND TWEET ID: " + tweetId);
        resp.setStatus(404);
        return;
      }
      summary = new TweetSummary.Builder(summary)
          .ignoreInTwittalyzer(bodyObj.getBoolean("ignore")).build();
      twitterService.save(summary);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    resp.setStatus(204);
  }
}
