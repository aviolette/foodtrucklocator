package foodtruck.server.vendor;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.Truck;
import foodtruck.model.TruckSchedule;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.socialmedia.ScheduleMessage;
import foodtruck.socialmedia.SocialMediaConnector;
import foodtruck.time.Clock;
import foodtruck.time.TimeOnlyFormatter;

/**
 * @author aviolette
 * @since 11/9/16
 */
@Singleton
public class PostScheduleServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(PostScheduleServlet.class.getName());
  private static final String JSP = "/WEB-INF/jsp/vendor/postSchedule.jsp";
  private final FoodTruckStopService service;
  private final Clock clock;
  private final DateTimeFormatter formatter;
  private final Set<SocialMediaConnector> connectors;

  @Inject
  public PostScheduleServlet(FoodTruckStopService stopService, Clock clock,
      @TimeOnlyFormatter DateTimeFormatter formatter,
      Set<SocialMediaConnector> socialMediaConnector) {
    this.service = stopService;
    this.clock = clock;
    this.formatter = formatter;
    this.connectors = socialMediaConnector;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    ScheduleMessage message = getScheduleMessage(truck);
    log.log(Level.INFO, "Posting {0} for truck {1} ", new Object[]{message, truck});
    for (SocialMediaConnector connector : connectors) {
      connector.updateStatusFor(message, truck);
    }
    resp.sendRedirect("/vendor");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    ScheduleMessage message = getScheduleMessage(truck);
    req.setAttribute("message", message);
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }

  private ScheduleMessage getScheduleMessage(@Nullable Truck truck) {
    TruckSchedule schedule = service.findStopsForDay(truck.getId(), clock.currentDay());
    return ScheduleMessage.builder()
        .formatter(formatter)
        .schedule(schedule)
        .build();
  }
}
