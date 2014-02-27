package foodtruck.server.dashboard;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.FoodTruckRequestDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.FoodTruckRequest;
import foodtruck.model.Location;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.Clock;
import foodtruck.util.FriendlyDateOnlyFormat;

/**
 * @author aviolette
 * @since 2/26/14
 */
@Singleton
public class TruckRequestServlet extends HttpServlet {
  private static final String JSP = "/WEB-INF/jsp/requestATruck.jsp";
  private final FoodTruckRequestDAO dao;
  private final GeoLocator geolocator;
  private final DateTimeFormatter formatter;
  private final Clock clock;

  @Inject
  public TruckRequestServlet(FoodTruckRequestDAO dao, GeoLocator geolocator, Clock clock,
      @FriendlyDateOnlyFormat DateTimeFormatter formatter) {
    this.dao = dao;
    this.geolocator = geolocator;
    this.formatter = formatter;
    this.clock = clock;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    int index = requestURI.indexOf("/admin/requests/edit/");
    String id = index == 0 ? requestURI.substring(21) : null;
    req.setAttribute("tab", "requests");
    req = new GuiceHackRequestWrapper(req, JSP);
    if (!Strings.isNullOrEmpty(id) && !"new".equals(id)) {
      FoodTruckRequest foodTruckRequest = dao.findById(Long.parseLong(id));
      req.setAttribute("foodTruckRequest", foodTruckRequest);
    }
    req.setAttribute("useEmail", true);
    req.setAttribute("title", "Request a Food Truck");
    req.getRequestDispatcher(JSP).forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    FoodTruckRequest.Builder builder = FoodTruckRequest.builder();
    String id = req.getParameter("id");
    if (!Strings.isNullOrEmpty(id)) {
      final long key = Long.parseLong(id);
      FoodTruckRequest existing = dao.findById(key);
      builder.key(key);
      builder.submitted(existing.getSubmitted());
      builder.approved(existing.getApproved());
    } else {
      builder.submitted(clock.now());
    }

    DateTime startTime = formatter.parseDateTime(req.getParameter("startDate")),
        endTime = formatter.parseDateTime(req.getParameter("startDate"));
    final Escaper escaper = HtmlEscapers.htmlEscaper();
    String address = req.getParameter("address");
    Location location = geolocator.locate(escaper.escape(address), GeolocationGranularity.NARROW);

    String description = req.getParameter("description");
    if (Strings.isNullOrEmpty(description)) {
      throw new ServletException("Description not specified");
    }

    description = escaper.escape(description);
    String requester = escaper.escape(req.getParameter("requester"));

    Number expected = 0;
    try {
      expected = NumberFormat.getInstance().parse(req.getParameter("expectedGuests"));
    } catch (ParseException e) {
    }

    FoodTruckRequest request = builder
        .description(description)
        .archived(false)
        .eventName(escaper.escape(req.getParameter("eventName")))
        .expectedGuests(expected.intValue())
        .prepaid("prepaid".equals(req.getParameter("prepaid")))
        .email(req.getParameter("email"))
        .userId(req.getParameter("email"))
        .requester(requester)
        .phone(escaper.escape(req.getParameter("phone")))
        .startTime(startTime)
        .endTime(endTime)
        .location(location)
        .build();
    long key = dao.save(request);
    resp.sendRedirect("/requests/view/" + key);
  }
}
