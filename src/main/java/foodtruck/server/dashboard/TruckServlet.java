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
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.StoryDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.Link;

/**
 * @author aviolette@gmail.com
 * @since 11/14/11
 */
@Singleton
public class TruckServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(TruckServlet.class.getName());
  private final FoodTruckStopService truckService;
  private final Clock clock;
  private final TruckDAO truckDAO;
  private final LocationDAO locationDAO;
  private final StoryDAO tweetDAO;
  private final DailyDataDAO dailyDataDAO;

  @Inject
  public TruckServlet(StoryDAO storyDAO, FoodTruckStopService truckService, Clock clock, TruckDAO truckDAO,
      LocationDAO locationDAO, DailyDataDAO dailyDataDAO) {
    this.tweetDAO = storyDAO;
    this.truckService = truckService;
    this.locationDAO = locationDAO;
    this.clock = clock;
    this.truckDAO = truckDAO;
    this.dailyDataDAO = dailyDataDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    req.setAttribute("localFrameworks", "true".equals(System.getProperty("use.local.frameworks", "false")));
    String truckId = requestURI.substring(14);
    if (Strings.isNullOrEmpty(truckId)) {
      resp.sendRedirect("/trucks");
      return;
    }
    if (truckId.endsWith("/configuration")) {
      truckId = truckId.substring(0, truckId.length() - 14);
      editConfiguration(truckId, req, resp);
    } else if (truckId.endsWith("/offtheroad")) {
      truckId = truckId.substring(0, truckId.length() - 11);
      offTheRoad(truckId, req, resp);
    } else {
      loadDashboard(truckId, req, resp);
    }
  }

  private void offTheRoad(String truckId, HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String jsp = "/WEB-INF/jsp/dashboard/offTheRoad.jsp";
    req = new GuiceHackRequestWrapper(req, jsp);
    final Truck truck = truckDAO.findById(truckId);
    req.setAttribute("truck", truck);
    req.setAttribute("nav", "trucks");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  private void editConfiguration(String truckId, HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    log.info("Loading configuration for " + truckId);
    final String jsp = "/WEB-INF/jsp/dashboard/truckEdit.jsp";
    // hack required when using * patterns in guice
    req = new GuiceHackRequestWrapper(req, jsp);
    req.setAttribute("headerName", "Edit");
    final Truck truck = truckDAO.findById(truckId);
    if (truck == null) {
      resp.sendError(404);
      return;
    }
    req.setAttribute("truck", truck);
    req.setAttribute("nav", "trucks");
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
    if (truck == null) {
      resp.setStatus(404);
      return;
    }
    final List<Story> stories = tweetDAO.findTweetsAfter(clock.currentDay().toDateTimeAtStartOfDay(clock.zone()),
        truck.getTwitterHandle(), true);

    req.setAttribute("tweets", stories);
    final String name = truck.getName();
    req.setAttribute("specials",  dailyDataDAO.findByTruckAndDay(truck.getId(), clock.currentDay()));
    req.setAttribute("headerName", name);
    req.setAttribute("truckId", truckId);
    req.setAttribute("truck", truck);
    req.setAttribute("nav", "trucks");
    req.setAttribute("localFrameworks", "true".equals(System.getProperty("use.local.frameworks", "false")));
    req.setAttribute("breadcrumbs", ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(name, "/admin/trucks" + truckId)));
    List<String> locationNames = ImmutableList.copyOf(
        Iterables.transform(locationDAO.findAutocompleteLocations(), Location.TO_NAME));
    req.setAttribute("locations", new JSONArray(locationNames).toString());
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (req.getRequestURI().endsWith("/configuration")) {
      handleConfigurationPost(req, resp);
    } else if (req.getRequestURI().endsWith("/offtheroad")) {
      handleOffTheRoadPost(req, resp);
    } else {
      handleTweetUpdate(req, resp);
    }
  }

  private void handleOffTheRoadPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String truckId = req.getRequestURI();
    truckId = truckId.substring(14, truckId.lastIndexOf('/'));
    truckService.offRoad(truckId, clock.currentDay());
    resp.sendRedirect("/admin/trucks");

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
    Truck.Builder builder = Truck.builder(truckDAO.findById(truckId));
    builder.id(truckId)
        .defaultCity(request.getParameter("defaultCity"))
        .description(request.getParameter("description"))
        .calendarUrl(request.getParameter("calendarUrl"))
        .menuUrl(request.getParameter("menuUrl"))
        .previewIcon(request.getParameter("previewIcon"))
        .fullsizeImage(request.getParameter("fullsizeImage"))
        .instagramId(request.getParameter("instagramId"))
        .facebook(request.getParameter("facebook"))
        .facebookPageId(request.getParameter("facebookPageId"))
        .foursquareUrl(request.getParameter("foursquareUrl"))
        .iconUrl(request.getParameter("iconUrl"))
        .backgroundImage(request.getParameter("backgroundImage"))
        .backgroundImageLarge(request.getParameter("backgroundImageLarge"))
        .timezoneOffset(Integer.parseInt(request.getParameter("timezoneAdjustment")))
        .name(request.getParameter("name"))
        .yelpSlug(request.getParameter("yelp"))
        .normalizePhone(request.getParameter("phone"))
        .email(request.getParameter("email"))
        .twitterHandle(request.getParameter("twitterHandle"))
        .url(request.getParameter("url"));
    final String[] optionsArray = request.getParameterValues("options");
    Set<String> options = ImmutableSet.copyOf(optionsArray == null ? new String[0] : optionsArray);
    builder.inactive(options.contains("inactive"));
    builder.twitterGeolocationDataValid(options.contains("twitterGeolocation"));
    builder.hidden(options.contains("hidden"));
    builder.useTwittalyzer(options.contains("twittalyzer"));
    builder.scanFacebook(options.contains("facebooker"));
    builder.allowSystemNotifications(options.contains("systemNotifications"));
    builder.displayEmailPublicly(options.contains("displayEmailPublicly"));
    String matchRegex = request.getParameter("matchOnlyIf");
    builder.matchOnlyIf(Strings.isNullOrEmpty(matchRegex) ? null : matchRegex);
    String doNotMatchRegex = request.getParameter("donotMatchIf");
    builder.donotMatchIf(Strings.isNullOrEmpty(doNotMatchRegex) ? null : doNotMatchRegex);
    Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
    builder.categories(ImmutableSet.copyOf(splitter.split(request.getParameter("categories"))));
    builder.beaconnaiseEmails(ImmutableSet.copyOf(splitter.split(request.getParameter("beaconnaiseEmails"))));
    try {
      builder.fleetSize(Integer.parseInt(request.getParameter("fleetSize")));
    } catch (NumberFormatException ignored) {
    }
    return builder.build();
  }

  private void handleTweetUpdate(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String body = new String(ByteStreams.toByteArray(req.getInputStream()));
    body = URLDecoder.decode(body, "UTF-8");
    try {
      JSONObject bodyObj = new JSONObject(body);
      final long tweetId = Long.parseLong(bodyObj.getString("id"));
      Story summary = tweetDAO.findByTweetId(tweetId);
      if (summary == null) {
        log.warning("COULDN'T FIND TWEET ID: " + tweetId);
        resp.setStatus(404);
        return;
      }
      summary = new Story.Builder(summary)
          .ignoreInTwittalyzer(bodyObj.getBoolean("ignore")).build();
      tweetDAO.saveOrUpdate(summary);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    resp.setStatus(204);

  }
}
