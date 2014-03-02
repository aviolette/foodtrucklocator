package foodtruck.server;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;


/**
 * @author aviolette
 * @since 5/20/13
 */
@Singleton
public class TrucksServlet extends FrontPageServlet {
  private final TruckDAO truckDAO;
  private final FoodTruckStopService stops;
  private final Clock clock;
  private final DateTimeZone zone;

  @Inject
  public TrucksServlet(ConfigurationDAO configurationDAO, TruckDAO trucks, FoodTruckStopService stops,
      Clock clock, DateTimeZone zone) {
    super(configurationDAO);
    this.truckDAO = trucks;
    this.stops = stops;
    this.clock = clock;
    this.zone = zone;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    String truckId = (requestURI.equals("/trucks") || requestURI.equals("/trucks/") ? null : requestURI.substring(8));
    String jsp = "/WEB-INF/jsp/trucks.jsp";
    if (!Strings.isNullOrEmpty(truckId)) {
      jsp = "/WEB-INF/jsp/truck.jsp";
      if (truckId.endsWith("/")) {
        truckId = truckId.substring(0, truckId.length() - 1);
      }
      Truck truck = truckDAO.findById(truckId);
      if (truck == null) {
        resp.sendError(404, "Page cannot be found: " + truckId);
        return;
      }
      req.setAttribute("truck", truck);
      LocalDate firstDay = clock.firstDayOfWeek();
      req.setAttribute("stops", stops.findSchedules(truck.getId(), new Interval(firstDay.toDateTimeAtStartOfDay(zone),
          firstDay.toDateTimeAtStartOfDay(zone).plusDays(7))));
      req.setAttribute("enableGraphs", configurationDAO.find().isShowPublicTruckGraphs());
      req.setAttribute("title", truck.getName());
    } else {
      String tag = req.getParameter("tag");
      final Collection<Truck> trucks = Strings.isNullOrEmpty(tag) ? truckDAO.findVisibleTrucks() :
          truckDAO.findByCategory(tag);
      if (!Strings.isNullOrEmpty(tag)) {
        req.setAttribute("filteredBy", tag);
      }
      req.setAttribute("foodTruckRequestOn", configurationDAO.find().isFoodTruckRequestOn());
      req.setAttribute("trucks", trucks);
      req.setAttribute("title", "Food Trucks in Chicago");
    }

    req = new GuiceHackRequestWrapper(req, jsp);
    req.setAttribute("tab", "trucks");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
