package foodtruck.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.server.resources.json.DailyScheduleWriter;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.DateOnlyFormatter;
import foodtruck.util.FriendlyDateOnlyFormat;

/**
 * @author aviolette
 * @since 8/27/14
 */
@Singleton
public class BoozeAndTrucksServlet extends FrontPageServlet {
  private static final String JSP = "/WEB-INF/jsp/booze.jsp";
  private static final Logger log = Logger.getLogger(BoozeAndTrucksServlet.class.getName());
  private final Clock clock;
  private final FoodTruckStopService stopService;
  private final DailyScheduleWriter dailyScheduleWriter;
  private final LocationDAO locationDAO;
  private final DateTimeFormatter dateFormatter;
  private final DateTimeFormatter friendlyFormatter;

  @Inject
  public BoozeAndTrucksServlet(ConfigurationDAO configDAO, FoodTruckStopService stopService, Clock clock,
                               @DateOnlyFormatter DateTimeFormatter dateFormatter,
                               @FriendlyDateOnlyFormat DateTimeFormatter friendlyFormatter,
                               DailyScheduleWriter scheduleWriter, LocationDAO locationDAO) {
    super(configDAO);
    this.stopService = stopService;
    this.clock = clock;
    this.dailyScheduleWriter = scheduleWriter;
    this.locationDAO = locationDAO;
    this.dateFormatter = dateFormatter;
    this.friendlyFormatter = friendlyFormatter;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    ImmutableList.Builder<ScheduleForDay> schedules = ImmutableList.builder();
    LocalDate currentDay = null;
    Map<String, TruckStopGroup> tsgs = Maps.newHashMap();
    String dateString = req.getParameter("date");
    LocalDate date = clock.currentDay();
    int daysOut = 7;
    if (!Strings.isNullOrEmpty(dateString)) {
      try {
        date = dateFormatter.parseDateTime(dateString).toLocalDate();
        daysOut = 1;
      } catch (IllegalArgumentException iae) {
      }
    }
    log.info("DATE: " + date);
    log.info("DATE2: " + date.toDateTimeAtStartOfDay());
    for (TruckStop stop : stopService.findUpcomingBoozyStops(date, daysOut)) {
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
    if (daysOut == 1) {
      req.setAttribute("title", "Boozy Stops for " + friendlyFormatter.print(date));
      req.setAttribute("boozyDate", date);
    } else {
      req.setAttribute("title", "Upcoming Boozy Events");
    }
    req.setAttribute("description", "Lists upcoming events that combine food trucks and booze.");
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
