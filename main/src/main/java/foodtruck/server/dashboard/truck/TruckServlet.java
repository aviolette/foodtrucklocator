package foodtruck.server.dashboard.truck;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.StoryDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.time.Clock;
import foodtruck.util.Link;

/**
 * @author aviolette@gmail.com
 * @since 11/14/11
 */
@Singleton
public class TruckServlet extends AbstractTruckServlet {
  private static final Logger log = Logger.getLogger(TruckServlet.class.getName());
  private final Clock clock;
  private final LocationDAO locationDAO;
  private final StoryDAO tweetDAO;
  private final DailyDataDAO dailyDataDAO;

  @Inject
  public TruckServlet(StoryDAO storyDAO, Clock clock, TruckDAO truckDAO,
      LocationDAO locationDAO, DailyDataDAO dailyDataDAO) {
    super(truckDAO);
    this.tweetDAO = storyDAO;
    this.locationDAO = locationDAO;
    this.clock = clock;
    this.dailyDataDAO = dailyDataDAO;
  }

  @Override
  protected ImmutableList<Link> breadcrumbs(Truck truck) {
    return ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(truck.getName(), "/admin/trucks/" + truck.getId()));
  }

  @Override
  protected void doGetProtected(HttpServletRequest request, HttpServletResponse response,
      Truck truck) throws ServletException, IOException {
    log.info("Loading dashboard for " + truck.getId());
    final List<Story> stories = tweetDAO.findTweetsAfter(clock.currentDay()
        .toDateTimeAtStartOfDay(clock.zone()), truck.getTwitterHandle(), true);
    request.setAttribute("tweets", stories);
    final String name = truck.getName();
    request.setAttribute("specials", dailyDataDAO.findByTruckAndDay(truck.getId(), clock.currentDay()));
    request.setAttribute("headerName", name);
    request.setAttribute("truckId", truck.getId());
    request.setAttribute("truck", truck);
    request.setAttribute("suffix", "-fluid");
    request.setAttribute("locations", locationDAO.findLocationNamesAsJson());
    forward(request, response);
  }

  @Override
  protected String getJsp() {
    return "/WEB-INF/jsp/dashboard/truck/truckDashboard.jsp";
  }
}
