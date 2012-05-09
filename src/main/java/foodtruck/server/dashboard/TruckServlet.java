package foodtruck.server.dashboard;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.model.TweetSummary;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.server.api.JsonWriter;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.twitter.TwitterService;
import foodtruck.util.Clock;
import foodtruck.util.Link;

/**
 * @author aviolette@gmail.com
 * @since 11/14/11
 */
@Singleton
public class TruckServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(TruckServlet.class.getName());
  private final TwitterService twitterService;
  private final FoodTruckStopService truckService;
  private final Clock clock;
  private final JsonWriter writer;
  private final TruckDAO truckDAO;

  @Inject
  public TruckServlet(TwitterService twitterService,
      FoodTruckStopService truckService, Clock clock, JsonWriter writer, TruckDAO truckDAO) {
    this.twitterService = twitterService;
    this.truckService = truckService;
    this.clock = clock;
    this.writer = writer;
    this.truckDAO = truckDAO;
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
    final Truck truck = truckDAO.findById(truckId);
    req.setAttribute("truck", truck);
    req.setAttribute("breadcrumbs", ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(truck.getName(), "/admin/trucks/" + truckId),
        new Link("Edit", "/admin/trucks/" + truckId + "/configuration")));
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  private void loadDashboard(String truckId, HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    log.info("Loading dashboard for " + truckId);
    final String jsp = "/WEB-INF/jsp/dashboard/truckDashboard.jsp";
    // hack required when using * patterns in guice
    req = new GuiceHackRequestWrapper(req, jsp);
    final Truck truck = truckDAO.findById(truckId);
    final List<TweetSummary> tweetSummaries =
        twitterService.findByTwitterHandle(truck.getTwitterHandle());
    req.setAttribute("tweets", tweetSummaries);
    final String name = truck.getName();
    req.setAttribute("headerName", name);
    req.setAttribute("truckId", truckId);
    req.setAttribute("truck", truck);
    req.setAttribute("breadcrumbs", ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(name, "/admin/trucks" + truckId)));
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

  private void handleConfigurationPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String contentType = req.getContentType();
    String truckId = req.getRequestURI().substring(14);
    if (truckId.endsWith("/configuration")) {
      truckId = truckId.substring(0, truckId.length() - 14);
      if ("application/x-www-form-urlencoded".equals(contentType)) {
        Truck truck = truckFromForm(req, truckId);
        truckDAO.save(truck);
        resp.sendRedirect("/admin/trucks/" + truckId);
      }
    }
  }

  private Truck truckFromForm(HttpServletRequest request, String truckId) {
    Truck.Builder builder = Truck.builder();
    builder.id(truckId)
        .key(truckId)
        .defaultCity(request.getParameter("defaultCity"))
        .description(request.getParameter("description"))
        .calendarUrl(request.getParameter("calendarUrl"))
        .facebook(request.getParameter("facebook"))
        .foursquareUrl(request.getParameter("foursquareUrl"))
        .iconUrl(request.getParameter("iconUrl"))
        .name(request.getParameter("name"))
        .twitterHandle(request.getParameter("twitterHandle"))
        .url(request.getParameter("url"));
    final String[] optionsArray = request.getParameterValues("options");
    Set<String> options = ImmutableSet.copyOf(optionsArray == null ? new String[0] : optionsArray);
    builder.inactive(options.contains("inactive"));
    builder.useTwittalyzer(options.contains("twittalyzer"));
    String matchRegex = request.getParameter("matchOnlyIf");
    builder.matchOnlyIf(Strings.isNullOrEmpty(matchRegex) ? null : matchRegex);
    String doNotMatchRegex = request.getParameter("donotMatchIf");
    builder.donotMatchIf(Strings.isNullOrEmpty(doNotMatchRegex) ? null : doNotMatchRegex);
    builder.categories(ImmutableSet
        .copyOf(Splitter.on(",").omitEmptyStrings().split(request.getParameter("categories"))));
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
