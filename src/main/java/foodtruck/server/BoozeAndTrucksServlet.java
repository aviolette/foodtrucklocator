package foodtruck.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.LocalDate;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.server.resources.json.DailyScheduleWriter;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 8/27/14
 */
@Singleton
public class BoozeAndTrucksServlet extends FrontPageServlet {
  private static final String JSP = "/WEB-INF/jsp/booze.jsp";
  private final Clock clock;
  private final FoodTruckStopService stopService;
  private final DailyScheduleWriter dailyScheduleWriter;
  private final LocationDAO locationDAO;

  @Inject
  public BoozeAndTrucksServlet(ConfigurationDAO configDAO, FoodTruckStopService stopService, Clock clock,
      DailyScheduleWriter scheduleWriter, LocationDAO locationDAO) {
    super(configDAO);
    this.stopService = stopService;
    this.clock = clock;
    this.dailyScheduleWriter = scheduleWriter;
    this.locationDAO = locationDAO;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    ImmutableList.Builder<ScheduleForDay> schedules = ImmutableList.builder();
    LocalDate currentDay = null;
    Map<String, TruckStopGroup> tsgs = Maps.newHashMap();
    for (TruckStop stop : stopService.findUpcomingBoozyStops(clock.currentDay())) {
      if (currentDay != null && !stop.getStartTime().toLocalDate().equals(currentDay) && !tsgs.isEmpty()) {
        schedules.add(new ScheduleForDay(currentDay, ImmutableList.copyOf(tsgs.values())));
        tsgs = Maps.newHashMap();
      }
      TruckStopGroup tsg = tsgs.get(stop.getLocation().getName());
      if (tsg == null) {
        tsg = new TruckStopGroup(stop.getLocation());
        tsgs.put(stop.getLocation().getName(), tsg);
      }
      tsg.addStop(stop);
      currentDay = stop.getStartTime().toLocalDate();
    }
    if (!tsgs.isEmpty()) {
      schedules.add(new ScheduleForDay(currentDay, ImmutableList.copyOf(tsgs.values())));
    }
    req.setAttribute("daySchedules", schedules.build());
    req.setAttribute("tab", "booze");
    req.setAttribute("title", "Upcoming Boozy Events");
    req.setAttribute("description", "Lists upcoming events that combine food trucks and booze.")
    req.getRequestDispatcher(JSP).forward(req, resp);
  }

  public static class TruckStopGroup {
    private Location location;
    private List<TruckStop> stops = Lists.newLinkedList();

    public TruckStopGroup(Location location) {
      this.location = location;
    }

    public Location getLocation() {
      return location;
    }

    public List<TruckStop> getStops() {
      return stops;
    }

    public void addStop(TruckStop stop) {
      this.stops.add(stop);
    }
  }

  public static class ScheduleForDay {
    private final LocalDate day;
    private final List<TruckStopGroup> groups;

    public ScheduleForDay(LocalDate day, List<TruckStopGroup> groups) {
      this.day = day;
      this.groups = groups;
    }

    public List<TruckStopGroup> getGroups() {
      return groups;
    }

    public LocalDate getDay() {
      return day;
    }
  }
}
