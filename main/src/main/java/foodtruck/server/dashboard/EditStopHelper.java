package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.time.Clock;
import foodtruck.time.HtmlDateFormatter;
import foodtruck.util.Link;

import static foodtruck.server.CodedServletException.NOT_FOUND;

/**
 * @author aviolette
 * @since 1/21/17
 */
public class EditStopHelper {

  private static final String ADMIN_JSP = "/WEB-INF/jsp/dashboard/editStop.jsp";
  private static final String VENDOR_JSP = "/WEB-INF/jsp/vendor/editStop.jsp";

  private final Clock clock;
  private final TruckStopDAO truckStopDAO;
  private final DateTimeFormatter timeFormatter;
  private final LocationDAO locationDAO;

  @Inject
  public EditStopHelper(Clock clock, @HtmlDateFormatter DateTimeFormatter formatter,
      LocationDAO locationDAO, TruckStopDAO truckStopDAO) {
    this.clock = clock;
    this.timeFormatter = formatter;
    this.locationDAO = locationDAO;
    this.truckStopDAO = truckStopDAO;
  }

  public void setupEditPage(String stopId, Truck truck, HttpServletRequest req, HttpServletResponse resp,
      boolean vendor) throws IOException, ServletException {
    String backUrl = vendor ? "/vendor" : "/admin/trucks/" + truck.getId();
    String jsp = vendor ? VENDOR_JSP : ADMIN_JSP;
    DateTime startTime, endTime;
    String title, locationName = "", description = "", imageUrl = "";
    boolean locked = false;
    req = new GuiceHackRequestWrapper(req, jsp);
    if ("new".equals(stopId)) {
      if (truck.getCategories()
          .contains("Breakfast") || clock.now()
          .getHourOfDay() > 11) {
        startTime = clock.now();
      } else {
        startTime = clock.now()
            .withTime(11, 0, 0, 0);
      }
      endTime = startTime.plusHours(3);
      endTime = endTime.withMinuteOfHour(0);
      title = "New Stop";
    } else {
      TruckStop stop = truckStopDAO.findByIdOpt(Long.parseLong(stopId)).orElseThrow(NOT_FOUND);
      locked = stop.isLocked();
      startTime = stop.getStartTime();
      endTime = stop.getEndTime();
      description = stop.getDescription();
      imageUrl = stop.getImageUrl();
      title = stop.getLocation()
          .getName();
      locationName = title;
    }
    if (req.getParameter("locked") != null) {
      locked = req.getParameter("locked")
          .equals("true");
    }
    if (req.getParameter("location") != null) {
      locationName = req.getParameter("location");
      title = locationName;
    }
    String endpoint = vendor ? "/vendor/stops/" + stopId : "/admin/trucks/" + truck.getId() + "/stops/" + stopId;
    if (req.getParameter("startTime") != null) {
      startTime = new DateTime(Long.parseLong(req.getParameter("startTime")));
    }
    if (req.getParameter("endTime") != null) {
      endTime = new DateTime(Long.parseLong(req.getParameter("endTime")));
    }
    req.setAttribute("startTime", timeFormatter.print(startTime));
    req.setAttribute("endTime", timeFormatter.print(endTime));
    req.setAttribute("description", description);
    req.setAttribute("imageUrl", imageUrl);
    req.setAttribute("baseEndPoint", vendor ? "/vendor" : "/admin");
    req.setAttribute("truck", truck);
    req.setAttribute("stopId", stopId);
    req.setAttribute("nav", "trucks");
    req.setAttribute("locked", locked);
    req.setAttribute("endpoint", endpoint);
    req.setAttribute("locationName", locationName);
    req.setAttribute("title", title);
    req.setAttribute("enableImages", true);
    req.setAttribute("backUrl", backUrl);
    req.setAttribute("locations", locationDAO.findLocationNamesAsJson());
    if (!vendor) {
      req.setAttribute("breadcrumbs", ImmutableList.of(new Link("Trucks", "/admin/trucks"),
          new Link(truck.getName(), "/admin/trucks/" + truck.getId()),
          new Link(title, "/admin/trucks/" + truck.getId() + "/stops/" + stopId)));
    }
    req.getRequestDispatcher(jsp)
        .forward(req, resp);
  }
}
