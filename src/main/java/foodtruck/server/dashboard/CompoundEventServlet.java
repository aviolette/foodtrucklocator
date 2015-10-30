package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.Clock;
import foodtruck.util.HtmlDateFormatter;
import foodtruck.util.TimeFormatter;

/**
 * @author aviolette
 * @since 1/8/15
 */
@Singleton
public class CompoundEventServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/compoundEvent.jsp";
  private final TruckDAO truckDAO;
  private final LocationDAO locationDAO;
  private final DateTimeFormatter timeFormatter;
  private final Clock clock;
  private final TruckStopDAO truckStopDAO;
  private final DateTimeFormatter urlFormatter;

  @Inject
  public CompoundEventServlet(TruckDAO truckDAO, LocationDAO locationDAO,
      @HtmlDateFormatter DateTimeFormatter formatter, Clock clock, TruckStopDAO truckStopDAO,
      @TimeFormatter DateTimeFormatter urlFormatter) {
    this.truckDAO = truckDAO;
    this.locationDAO = locationDAO;
    this.timeFormatter = formatter;
    this.clock = clock;
    this.truckStopDAO = truckStopDAO;
    this.urlFormatter = urlFormatter;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String startTimeParam = req.getParameter("startTime"),
        endTimeParam = req.getParameter("endTime"),
        locationID = req.getRequestURI().substring(req.getRequestURI().lastIndexOf('/') + 1);
    String[] trucks = req.getParameterValues("trucks");
    Location location = locationDAO.findById(Long.valueOf(locationID));
    if (location == null) {
      resp.sendError(404);
      return;
    }
    DateTime startTime = timeFormatter.parseDateTime(startTimeParam),
        endTime = timeFormatter.parseDateTime(endTimeParam);

    if (!endTime.isAfter(startTime)) {
      resp.sendError(400, "End time is not after start time");
      return;
    }

    for (String truckId : trucks) {
      TruckStop stop = TruckStop.builder()
          .location(location)
          .startTime(startTime)
          .endTime(endTime)
          .origin(StopOrigin.MANUAL)
          .truck(truckDAO.findById(truckId))
          .build();
      truckStopDAO.save(stop);
    }
    resp.sendRedirect("/admin/trucks");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String locationID = req.getRequestURI().substring(req.getRequestURI().lastIndexOf('/') + 1);
    Location location = locationDAO.findById(Long.valueOf(locationID));
    if (location == null) {
      resp.sendError(404);
      return;
    }
    DateTime startTime = clock.now().plusHours(1).withMinuteOfHour(0), endTime = startTime.plusHours(2);
    if (clock.now().isBefore(clock.timeAt(11, 0))) {
      startTime = clock.timeAt(11, 0);
      endTime = clock.timeAt(14, 0);
    }
    try {
      startTime = urlFormatter.parseDateTime(req.getParameter("startTime"));
      endTime = urlFormatter.parseDateTime(req.getParameter("endTime"));
    } catch (IllegalArgumentException|NullPointerException ignored) {
    }
    final ImmutableSet<String> selected = Strings.isNullOrEmpty(req.getParameter("selected")) ? ImmutableSet.<String>of() :
        ImmutableSet.copyOf(req.getParameter("selected").split(","));
    req.setAttribute("startTime", timeFormatter.print(startTime));
    req.setAttribute("endTime", timeFormatter.print(endTime));
    req.setAttribute("location", location);
    req.setAttribute("postMethod", req.getRequestURI());
    req.setAttribute("trucks", ImmutableList.copyOf(
        Iterables.transform(truckDAO.findActiveTrucks(), new Function<Truck, TruckWithSelectionIndicator>() {
              public TruckWithSelectionIndicator apply(Truck truck) {
                return new TruckWithSelectionIndicator(truck, selected.contains(truck.getId()));
              }
            })));
    req.setAttribute("nav", "location");
    req = new GuiceHackRequestWrapper(req, JSP_PATH);
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }

  public static class TruckWithSelectionIndicator {
    private final Truck truck;
    private final boolean selected;

    public TruckWithSelectionIndicator(Truck truck, boolean selected) {
      this.truck = truck;
      this.selected = selected;
    }

    public Truck getTruck() {
      return truck;
    }

    public boolean isSelected() {
      return selected;
    }
  }
}
