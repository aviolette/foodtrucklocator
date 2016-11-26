package foodtruck.server.dashboard.truck;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
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
import foodtruck.util.Clock;
import foodtruck.util.Link;

/**
 * @author aviolette@gmail.com
 * @since 11/14/11
 */
@Singleton
public class TruckServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(TruckServlet.class.getName());
  private final Clock clock;
  private final TruckDAO truckDAO;
  private final LocationDAO locationDAO;
  private final StoryDAO tweetDAO;
  private final DailyDataDAO dailyDataDAO;

  @Inject
  public TruckServlet(StoryDAO storyDAO, Clock clock, TruckDAO truckDAO,
      LocationDAO locationDAO, DailyDataDAO dailyDataDAO) {
    this.tweetDAO = storyDAO;
    this.locationDAO = locationDAO;
    this.clock = clock;
    this.truckDAO = truckDAO;
    this.dailyDataDAO = dailyDataDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    String truckId = requestURI.substring(14);
    if (Strings.isNullOrEmpty(truckId)) {
      resp.sendRedirect("/trucks");
      return;
    }
    loadDashboard(truckId, req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    handleTweetUpdate(req, resp);
  }


  private void loadDashboard(String truckId, HttpServletRequest req,
      HttpServletResponse resp) throws IOException, ServletException {
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
    req.setAttribute("specials", dailyDataDAO.findByTruckAndDay(truck.getId(), clock.currentDay()));
    req.setAttribute("headerName", name);
    req.setAttribute("truckId", truckId);
    req.setAttribute("truck", truck);
    req.setAttribute("suffix", "-fluid");
    req.setAttribute("nav", "trucks");
    req.setAttribute("breadcrumbs",
        ImmutableList.of(new Link("Trucks", "/admin/trucks"), new Link(name, "/admin/trucks" + truckId)));
    req.setAttribute("locations", locationNamesAsJsonArray());
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  private String locationNamesAsJsonArray() {
    List<String> locationNames = ImmutableList.copyOf(
        Iterables.transform(locationDAO.findAutocompleteLocations(), Location.TO_NAME));
    return new JSONArray(locationNames).toString();
  }


  private void handleTweetUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
      summary = new Story.Builder(summary).ignoreInTwittalyzer(bodyObj.getBoolean("ignore")).build();
      tweetDAO.saveOrUpdate(summary);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    resp.setStatus(204);
  }
}
