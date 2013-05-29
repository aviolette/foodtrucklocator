package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.EventDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Event;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.Clock;
import foodtruck.util.TimeFormatter;

/**
 * @author aviolette
 * @since 5/28/13
 */
@Singleton
public class EventServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/editEvent.jsp";
  private final EventDAO dao;
  private final TruckDAO truckDAO;
  private final GeoLocator geoLocator;
  private final DateTimeFormatter formatter;
  private final Clock clock;

  @Inject
  public EventServlet(Clock clock, EventDAO dao, TruckDAO truckDAO, GeoLocator locator,
      @TimeFormatter DateTimeFormatter formatter) {
    this.dao = dao;
    this.truckDAO = truckDAO;
    this.geoLocator = locator;
    this.formatter = formatter;
    this.clock = clock;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String eventId = req.getRequestURI().substring(14);
    Event event;
    if ("new".equals(eventId)) {
      DateTime now = clock.now();
      event = Event.builder().startTime(now).endTime(now.plusHours(2)).build();
    } else {
      event = dao.findById(eventId);
    }
    req = new GuiceHackRequestWrapper(req, JSP_PATH);
    req.setAttribute("event", event);
    req.setAttribute("nav", "events");
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String eventId = req.getRequestURI().substring(14);
    if ("Delete".equals(req.getParameter("action"))) {
      dao.delete(eventId);
      resp.sendRedirect("/admin/events");
      return;
    }
    String truckRequest = req.getParameter("trucks");
    Iterable<String> truckList = Splitter.on(",")
        .omitEmptyStrings().trimResults().split(truckRequest);
    List<Truck> trucks = FluentIterable.from(truckList)
        .transform(new Function<String, Truck>() {
          @Nullable @Override public Truck apply(String truckId) {
            Truck t = truckDAO.findById(truckId);
            return t;
          }
        }).toList();
    Location location = geoLocator.locate(req.getParameter("location"), GeolocationGranularity.BROAD);
    DateTime startTime = formatter.parseDateTime(req.getParameter("startTime")),
        endTime = formatter.parseDateTime(req.getParameter("endTime"));
    Event event = Event.builder()
        .description(Strings.nullToEmpty(req.getParameter("description")))
        .url(req.getParameter("url"))
        .trucks(trucks)
        .location(location)
        .startTime(startTime)
        .endTime(endTime)
        .name(req.getParameter("name"))
        .key(req.getParameter("id"))
        .build();
    dao.save(event);
    resp.sendRedirect("/admin/events/" + event.getKey());
  }
}
