package foodtruck.server;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;


/**
 * @author aviolette
 * @since 5/20/13
 */
@Singleton
public class TrucksServlet extends HttpServlet {
  private final TruckDAO truckDAO;
  private final FoodTruckStopService stops;
  private final Clock clock;
  private final DateTimeZone zone;

  @Inject
  public TrucksServlet(TruckDAO trucks, FoodTruckStopService stops, Clock clock, DateTimeZone zone) {
    this.truckDAO = trucks;
    this.stops = stops;
    this.clock = clock;
    this.zone = zone;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    String truckId = (requestURI.equals("/trucks") || requestURI.equals("/trucks/") ? null : requestURI.substring(8));
    String jsp = "/WEB-INF/jsp/trucks.jsp";
    req = new GuiceHackRequestWrapper(req, jsp);
    req.setAttribute("tab", "trucks");
    final Collection<Truck> trucks = truckDAO.findVisibleTrucks();
    req.setAttribute("trucks", trucks);
    if (!Strings.isNullOrEmpty(truckId)) {
      Truck truck = truckDAO.findById(truckId);
      req.setAttribute("truck", truck);
      LocalDate firstDay = clock.firstDayOfWeek();
      req.setAttribute("stops", stops.findSchedules(truck.getId(), new Interval(firstDay.toDateTimeAtStartOfDay(zone),
          firstDay.toDateTimeAtStartOfDay(zone).plusDays(7))));
    }
    req.setAttribute("containerType", "fixed");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}