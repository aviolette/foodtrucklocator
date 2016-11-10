package foodtruck.server.vendor;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.model.TruckSchedule;
import foodtruck.schedule.ScheduleMessage;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.socialmedia.SocialMediaConnector;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.TimeOnlyFormatter;

/**
 * @author aviolette
 * @since 11/9/16
 */
@Singleton
public class PostScheduleServlet extends VendorServletSupport {
  private static final Logger log = Logger.getLogger(PostScheduleServlet.class.getName());
  private static final String JSP = "/WEB-INF/jsp/vendor/postSchedule.jsp";
  private final FoodTruckStopService service;
  private final Clock clock;
  private final DateTimeFormatter formatter;
  private final Set<SocialMediaConnector> connectors;

  @Inject
  public PostScheduleServlet(TruckDAO dao, UserService userService, Provider<SessionUser> sessionUserProvider,
      FoodTruckStopService stopService, Clock clock, @TimeOnlyFormatter DateTimeFormatter formatter,
      Set<SocialMediaConnector> socialMediaConnector) {
    super(dao, userService, sessionUserProvider);
    this.service = stopService;
    this.clock = clock;
    this.formatter = formatter;
    this.connectors = socialMediaConnector;
  }

  @Override
  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, String truckId) throws IOException {
    super.dispatchPost(req, resp, truckId);
    Truck truck = truckDAO.findById(truckId);
    ScheduleMessage message = getScheduleMessage(truck);
    log.log(Level.INFO, "Posting {0} for truck {1} ", new Object[]{message, truck});
    for (SocialMediaConnector connector : connectors) {
      connector.updateStatusFor(message, truck);
    }
    // TODO: flash with message
    resp.sendRedirect("/vendor");
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Truck truck) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
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
