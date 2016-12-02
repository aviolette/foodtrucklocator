package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.time.Clock;
import foodtruck.time.HtmlDateFormatter;
import foodtruck.time.TimeFormatter;

/**
 * @author aviolette
 * @since 7/12/16
 */
@RequestScoped
public class EventServletSupport {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/compoundEvent.jsp";
  private final Clock clock;
  private final DateTimeFormatter urlFormatter;
  private final DateTimeFormatter timeFormatter;
  private final TruckDAO truckDAO;
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final FoodTruckStopService stopService;

  @Inject
  public EventServletSupport(HttpServletRequest request, HttpServletResponse response, Clock clock, 
      @TimeFormatter DateTimeFormatter urlFormatter, @HtmlDateFormatter DateTimeFormatter formatter,
      TruckDAO truckDAO, FoodTruckStopService stopService) {
    this.request = request;
    this.response = response;
    this.clock = clock;
    this.urlFormatter = urlFormatter;
    this.timeFormatter = formatter;
    this.truckDAO = truckDAO;
    this.stopService = stopService;
  }

  public void post(Location location, String principal, String redirectTo) throws IOException {
    String startTimeParam = request.getParameter("startTime"),
        endTimeParam = request.getParameter("endTime");
    String[] trucks = request.getParameterValues("trucks");
    DateTime startTime = timeFormatter.parseDateTime(startTimeParam),
        endTime = timeFormatter.parseDateTime(endTimeParam);
    if (!endTime.isAfter(startTime)) {
      response.sendError(400, "End time is not after start time");
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
      stopService.update(stop, principal);
    }
    response.sendRedirect(redirectTo);
  }

  public void get(Location location, String redirectTo) throws ServletException, IOException {
    DateTime startTime = clock.now().plusHours(1).withMinuteOfHour(0), endTime = startTime.plusHours(2);
    if (clock.now().isBefore(clock.timeAt(11, 0))) {
      startTime = clock.timeAt(11, 0);
      endTime = clock.timeAt(14, 0);
    }
    try {
      startTime = urlFormatter.parseDateTime(request.getParameter("startTime"));
      endTime = urlFormatter.parseDateTime(request.getParameter("endTime"));
    } catch (IllegalArgumentException|NullPointerException ignored) {
    }
    final ImmutableSet<String> selected = Strings.isNullOrEmpty(request.getParameter("selected")) ? ImmutableSet.<String>of() :
        ImmutableSet.copyOf(request.getParameter("selected").split(","));
    request.setAttribute("startTime", timeFormatter.print(startTime));
    request.setAttribute("endTime", timeFormatter.print(endTime));
    request.setAttribute("location", location);
    request.setAttribute("cancelUrl", redirectTo);
    request.setAttribute("postMethod", request.getRequestURI());
    request.setAttribute("trucks", ImmutableList.copyOf(
        Iterables.transform(truckDAO.findActiveTrucks(), new Function<Truck, TruckWithSelectionIndicator>() {
          public TruckWithSelectionIndicator apply(Truck truck) {
            return new TruckWithSelectionIndicator(truck, selected.contains(truck.getId()));
          }
        })));
    request.setAttribute("nav", "location");
    HttpServletRequest req = new GuiceHackRequestWrapper(request, JSP_PATH);
    request.getRequestDispatcher(JSP_PATH).forward(req, response);
  }

  @SuppressWarnings("WeakerAccess")
  public static class TruckWithSelectionIndicator {
    private final Truck truck;
    private final boolean selected;

    TruckWithSelectionIndicator(Truck truck, boolean selected) {
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
