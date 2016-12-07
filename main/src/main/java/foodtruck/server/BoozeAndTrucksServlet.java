package foodtruck.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.time.Clock;
import foodtruck.time.DateOnlyFormatter;
import foodtruck.time.FriendlyDateOnlyFormat;

/**
 * @author aviolette
 * @since 8/27/14
 */
@Singleton
public class BoozeAndTrucksServlet extends HttpServlet {
  private static final String JSP = "/WEB-INF/jsp/booze.jsp";
  private final Clock clock;
  private final FoodTruckStopService stopService;
  private final DateTimeFormatter dateFormatter;
  private final DateTimeFormatter friendlyFormatter;
  private final LocationDAO locationDAO;

  @Inject
  public BoozeAndTrucksServlet(FoodTruckStopService stopService, Clock clock,
      @DateOnlyFormatter DateTimeFormatter dateFormatter, @FriendlyDateOnlyFormat DateTimeFormatter friendlyFormatter,
      LocationDAO locationDAO) {
    this.stopService = stopService;
    this.clock = clock;
    this.dateFormatter = dateFormatter;
    this.locationDAO = locationDAO;
    this.friendlyFormatter = friendlyFormatter;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    ImmutableList.Builder<ScheduleForDay> schedules = ImmutableList.builder();
    LocalDate currentDay = null;
    Map<String, TruckStopGroup> tsgs = Maps.newHashMap();
    String dateString = req.getParameter("date");
    LocalDate date = clock.currentDay();
    LoadingCache<String, Location> locationCache = CacheBuilder.newBuilder()
        .maximumSize(200)
        .build(new CacheLoader<String, Location>() {
          public Location load(String key) throws Exception {
            return locationDAO.findByAddress(key);
          }
        });
    int daysOut = 7;
    if (!Strings.isNullOrEmpty(dateString)) {
      try {
        date = dateFormatter.parseDateTime(dateString)
            .toLocalDate();
        daysOut = 1;
      } catch (IllegalArgumentException ignored) {
      }
    }
    ImmutableList.Builder<TruckStopGroup> builder = ImmutableList.builder();
    for (TruckStop stop : stopService.findUpcomingBoozyStops(date, daysOut)) {
      if (currentDay != null && !stop.getStartTime()
          .toLocalDate()
          .equals(currentDay) && !tsgs.isEmpty()) {
        schedules.add(new ScheduleForDay(currentDay, ImmutableList.copyOf(tsgs.values())));
        tsgs = Maps.newHashMap();
      }
      TruckStopGroup tsg = tsgs.get(stop.getLocation()
          .getName());
      currentDay = stop.getStartTime()
          .toLocalDate();
      if (tsg == null) {
        Location location;
        try {
          location = locationCache.get(stop.getLocation()
              .getName());
        } catch (ExecutionException e) {
          location = stop.getLocation();
        }
        tsg = new TruckStopGroup(location, currentDay);
        tsgs.put(stop.getLocation()
            .getName(), tsg);
        builder.add(tsg);
      }
      tsg.addStop(stop);
    }
    if (!tsgs.isEmpty()) {
      schedules.add(new ScheduleForDay(currentDay, ImmutableList.copyOf(tsgs.values())));
    }
    req.setAttribute("allGroups", builder.build());
    req.setAttribute("tab", "location");
    req.setAttribute("daySchedules", schedules.build());

    if (daysOut == 1) {
      req.setAttribute("title", "Boozy Stops for " + friendlyFormatter.print(date));
      req.setAttribute("boozyDate", date);
    } else {
      req.setAttribute("title", "Upcoming Boozy Events");
    }
    req.setAttribute("description", "Lists upcoming events that combine food trucks and booze.");
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }

  public static class TruckStopGroup {
    private Location location;
    private LocalDate day;
    private List<TruckStop> stops = Lists.newLinkedList();

    public TruckStopGroup(Location location, LocalDate day) {
      this.location = location;
      this.day = day;
    }

    public LocalDate getDay() {
      return day;
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

    @SuppressWarnings("unused")
    public List<TruckStopGroup> getGroups() {
      return groups;
    }

    public LocalDate getDay() {
      return day;
    }
  }
}
