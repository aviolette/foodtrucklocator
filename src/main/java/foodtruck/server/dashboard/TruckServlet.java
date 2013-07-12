package foodtruck.server.dashboard;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.DailySchedule;
import foodtruck.model.DayOfWeek;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStop;
import foodtruck.model.TweetSummary;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.twitter.TwitterService;
import foodtruck.util.Clock;
import foodtruck.util.Link;
import foodtruck.util.MoreStrings;

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
  private final TruckDAO truckDAO;
  private final DateTimeZone zone;
  private final LocationDAO locationDAO;

  @Inject
  public TruckServlet(TwitterService twitterService, DateTimeZone zone,
      FoodTruckStopService truckService, Clock clock, TruckDAO truckDAO, LocationDAO locationDAO) {
    this.twitterService = twitterService;
    this.truckService = truckService;
    this.locationDAO = locationDAO;
    this.clock = clock;
    this.truckDAO = truckDAO;
    this.zone = zone;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    String truckId = requestURI.substring(14);
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
    final List<TweetSummary> tweetSummaries =
        twitterService.findByTwitterHandle(truck.getTwitterHandle());
    req.setAttribute("tweets", tweetSummaries);
    final String name = truck.getName();
    req.setAttribute("headerName", name);
    req.setAttribute("truckId", truckId);
    req.setAttribute("truck", truck);
    req.setAttribute("nav", "trucks");
    req.setAttribute("breadcrumbs", ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(name, "/admin/trucks" + truckId)));

    DateTime current = clock.now();
    int dayOfWeek = current.getDayOfWeek();
    final DateTime mondayPrior = current.minusDays(6 + dayOfWeek);
    final DateTime nextSunday = current.plusDays(7 - dayOfWeek + 1);
    List<DailySchedule> schedules = truckService.findSchedules(truck.getId(),
        new Interval(mondayPrior, nextSunday));
    PriorAndCurrentSchedule schedule[] = new PriorAndCurrentSchedule[7];
    final DateTime mondayCurrent = current.minusDays(dayOfWeek - 1);
    for (int day = 0; day < 7; day++) {
      schedule[day] = findBoth(mondayCurrent.plusDays(day).toLocalDate(),
          mondayPrior.plusDays(day).toLocalDate(), schedules, day);
    }
    req.setAttribute("schedule", Arrays.asList(schedule));
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
    TruckSchedule stops = truckService.findStopsForDay(truckId, clock.currentDay());
    for (TruckStop stop : stops.getStops()) {
      truckService.delete((Long) stop.getKey());
    }
    Truck t = truckDAO.findById(truckId);
    t = Truck.builder(t).muteUntil(clock.currentDay().toDateMidnight(zone).toDateTime().plusDays(1))
        .build();
    truckDAO.save(t);
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
        .facebook(request.getParameter("facebook"))
        .facebookPageId(request.getParameter("facebookPageId"))
        .foursquareUrl(request.getParameter("foursquareUrl"))
        .iconUrl(request.getParameter("iconUrl"))
        .name(request.getParameter("name"))
        .yelpSlug(request.getParameter("yelp"))
        .phone(request.getParameter("phone"))
        .email(request.getParameter("email"))
        .twitterHandle(request.getParameter("twitterHandle"))
        .url(request.getParameter("url"));
    final String[] optionsArray = request.getParameterValues("options");
    Set<String> options = ImmutableSet.copyOf(optionsArray == null ? new String[0] : optionsArray);
    builder.inactive(options.contains("inactive"));
    builder.twitterGeolocationDataValid(options.contains("twitterGeolocation"));
    builder.hidden(options.contains("hidden"));
    builder.useTwittalyzer(options.contains("twittalyzer"));
    String matchRegex = request.getParameter("matchOnlyIf");
    builder.matchOnlyIf(Strings.isNullOrEmpty(matchRegex) ? null : matchRegex);
    String doNotMatchRegex = request.getParameter("donotMatchIf");
    builder.donotMatchIf(Strings.isNullOrEmpty(doNotMatchRegex) ? null : doNotMatchRegex);
    builder.categories(ImmutableSet
        .copyOf(Splitter.on(",").omitEmptyStrings().trimResults().split(request.getParameter("categories"))));
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

  private PriorAndCurrentSchedule findBoth(LocalDate current, LocalDate prior,
      List<DailySchedule> schedules, int day) {
    DailySchedule currentSchedule = null, priorSchedule = null;
    for (DailySchedule schedule : schedules) {
      if (current.equals(schedule.getDay())) {
        currentSchedule = schedule;
      } else if (prior.equals(schedule.getDay())) {
        priorSchedule = schedule;
      }
    }
    return new PriorAndCurrentSchedule(currentSchedule, priorSchedule, DayOfWeek.fromConstant(day));
  }

  public static class PriorAndCurrentSchedule {
    private final DailySchedule currentSchedule;
    private final DailySchedule priorSchedule;
    private final DayOfWeek dayOfWeek;

    public PriorAndCurrentSchedule(DailySchedule currentSchedule, DailySchedule priorSchedule,
        DayOfWeek dayOfWeek) {
      this.currentSchedule = currentSchedule;
      this.priorSchedule = priorSchedule;
      this.dayOfWeek = dayOfWeek;
    }

    public DailySchedule getCurrent() {
      return currentSchedule;
    }

    public DailySchedule getPrior() {
      return priorSchedule;
    }

    public String getName() {
      return MoreStrings.capitalize(dayOfWeek.toString());
    }
  }
}
