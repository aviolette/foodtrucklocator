package foodtruck.server;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.DailySchedule;
import foodtruck.model.StaticConfig;
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
      Clock clock, DateTimeZone zone, StaticConfig staticConfig) {
    super(configurationDAO, staticConfig);
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
      req.setAttribute("stops", getSchedules(truck, firstDay));
      req.setAttribute("daysOfWeek", daysOfWeek(firstDay));
      req.setAttribute("enableGraphs", configurationDAO.find().isShowPublicTruckGraphs());
      req.setAttribute("title", truck.getName());
      req.setAttribute("description", Strings.isNullOrEmpty(truck.getDescription()) ? truck.getName() : truck.getDescription());
    } else {
      String tag = req.getParameter("tag");
      if (!Strings.isNullOrEmpty(tag)) {
        req.setAttribute("filteredBy", tag);
      }
      req.setAttribute("foodTruckRequestOn", configurationDAO.find().isFoodTruckRequestOn());
      req.setAttribute("description", "Catalogue of all the food trucks in Chicago-land");
      req.setAttribute("title", "Food Trucks in Chicago");
    }

    req = new GuiceHackRequestWrapper(req, jsp);
    req.setAttribute("tab", "trucks");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  private List<DailySchedule> getSchedules(Truck truck, LocalDate firstDay) {
    return stops.findSchedules(truck.getId(), new Interval(firstDay.toDateTimeAtStartOfDay(zone),
        firstDay.toDateTimeAtStartOfDay(zone).plusDays(8)));
  }

  private Iterable<String> daysOfWeek(LocalDate firstDay) {
    DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MM/dd");
    ImmutableList.Builder builder = ImmutableList.builder();
    LocalDate date = firstDay;
    for (int i=0; i < 7; i++) {
      builder.add(formatter.print(date));
      date = date.plusDays(1);
    }
    return builder.build();
  }

}
