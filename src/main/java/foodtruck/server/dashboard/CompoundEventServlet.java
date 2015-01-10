package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.Clock;
import foodtruck.util.MilitaryTimeOnlyFormatter;

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

  @Inject
  public CompoundEventServlet(TruckDAO truckDAO, LocationDAO locationDAO,
      @MilitaryTimeOnlyFormatter DateTimeFormatter formatter, Clock clock, TruckStopDAO truckStopDAO) {
    this.truckDAO = truckDAO;
    this.locationDAO = locationDAO;
    this.timeFormatter = formatter;
    this.clock = clock;
    this.truckStopDAO = truckStopDAO;
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
    LocalDate now = clock.currentDay();
    DateTime startTime = now.toDateTime(timeFormatter.parseLocalTime(startTimeParam), clock.zone()),
        endTime = now.toDateTime(timeFormatter.parseLocalTime(endTimeParam), clock.zone());
    for (String truckId : trucks) {
      TruckStop stop = TruckStop.builder()
          .location(location)
          .startTime(startTime)
          .endTime(endTime)
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
    req.setAttribute("location", location);
    req.setAttribute("trucks", truckDAO.findActiveTrucks());
    req.setAttribute("nav", "location");
    req = new GuiceHackRequestWrapper(req, JSP_PATH);
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }
}
