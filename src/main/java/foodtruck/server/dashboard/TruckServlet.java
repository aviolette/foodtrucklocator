package foodtruck.server.dashboard;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Truck;
import foodtruck.model.Trucks;
import foodtruck.model.TweetSummary;
import foodtruck.server.GuiceHackRequestWrapper;
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
    String truckId = requestURI.substring(14);
    if (truckId.endsWith("/configuration")) {
      truckId = truckId.substring(0, truckId.length() - 14);
      editConfiguration(truckId, req, resp);
    } else {
      loadDashboard(truckId, req, resp);
    }
  }

  private void editConfiguration(String truckId, HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    log.info("Loading configuration for " + truckId);
    final String jsp = "/WEB-INF/jsp/dashboard/truckEdit.jsp";
    // hack required when using * patterns in guice
    req = new GuiceHackRequestWrapper(req, jsp);
    req.setAttribute("headerName", "Edit");
    req.setAttribute("truck", trucks.findById(truckId));
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  private void loadDashboard(String truckId, HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    log.info("Loading dashboard for " + truckId);
    final List<TweetSummary> tweetSummaries = twitterService.findForTruck(truckId);
    req.setAttribute("tweets", tweetSummaries);
    final String jsp = "/WEB-INF/jsp/dashboard/truckDashboard.jsp";
    // hack required when using * patterns in guice
    req = new GuiceHackRequestWrapper(req, jsp);
    req.setAttribute("headerName", trucks.findById(truckId).getName());
    req.setAttribute("truckId", truckId);
    try {
      req.setAttribute("schedule",
          writer.writeSchedule(truckService.findStopsForDay(truckId, clock.currentDay())));
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    req.getRequestDispatcher(jsp).forward(req, resp);

  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (req.getRequestURI().endsWith("/configuration")) {
      handleConfigurationPost(req, resp);
    } else {
      handleTweetUpdate(req, resp);
    }
  }

  private void handleConfigurationPost(HttpServletRequest req, HttpServletResponse resp) {
    String contentType = req.getContentType();
    String truckId = req.getRequestURI().substring(14);
    if (truckId.endsWith("/configuration")) {
      truckId = truckId.substring(0, truckId.length() - 14);
    }
    if ("application/x-www-form-urlencoded".equals(contentType)) {
      Truck truck = truckFromForm(req.getParameterMap(), truckId);
      System.out.println("HERE");
    }
  }

  private Truck truckFromForm(Map parameterMap, String truckId) {
    Truck.Builder builder = Truck.builder();
    builder.id(truckId)
        .url((String) parameterMap.get("url"));
    return builder.build();
  }

  private void handleTweetUpdate(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
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
