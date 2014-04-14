package foodtruck.server.delivery;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.FoodTruckRequestDAO;
import foodtruck.email.EmailNotifier;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.FoodTruckRequest;
import foodtruck.model.Location;
import foodtruck.server.FrontPageServlet;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.Clock;
import foodtruck.util.FriendlyDateOnlyFormat;

/**
 * @author aviolette
 * @since 12/20/13
 */
@Singleton
public class RequestATruckServlet extends FrontPageServlet {
  private static final String JSP = "/WEB-INF/jsp/requestATruck.jsp";
  private final FoodTruckRequestDAO foodTruckRequestDAO;
  private final GeoLocator geoLocator;
  private final DateTimeFormatter formatter;
  private final Clock clock;
  private final EmailNotifier notifier;

  @Inject
  public RequestATruckServlet(ConfigurationDAO configDAO, FoodTruckRequestDAO foodTruckRequestDAO,
      GeoLocator geoLocator, @FriendlyDateOnlyFormat DateTimeFormatter formatter, Clock clock,
      EmailNotifier emailNotifier) {
    super(configDAO);
    this.foodTruckRequestDAO = foodTruckRequestDAO;
    this.geoLocator = geoLocator;
    this.formatter = formatter;
    this.clock = clock;
    this.notifier = emailNotifier;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (!configurationDAO.find().isFoodTruckRequestOn()) {
      resp.setStatus(404);
      return;
    }
    final String requestURI = req.getRequestURI();
    int index = requestURI.indexOf("/requests/edit/");
    String id = index == 0 ? requestURI.substring(15) : null;
    req.setAttribute("tab", "trucks");
    req = new GuiceHackRequestWrapper(req, JSP);
    if (!Strings.isNullOrEmpty(id) && !"new".equals(id)) {
      FoodTruckRequest foodTruckRequest = foodTruckRequestDAO.findById(Long.parseLong(id));
      if (foodTruckRequest == null) {
        resp.setStatus(404);
        return;
      } else if (!canEdit(UserServiceFactory.getUserService(), foodTruckRequest)) {
        resp.setStatus(403);
        return;
      } else {
        req.setAttribute("foodTruckRequest", foodTruckRequest);
      }
    }
    req.setAttribute("title", "Request a Food Truck");
    req.setAttribute("email", UserServiceFactory.getUserService().getCurrentUser().getEmail());
    req.getRequestDispatcher(JSP).forward(req, resp);
  }

  private boolean canEdit(UserService userService, FoodTruckRequest request) {
    return userService.isUserAdmin() || request.getEmail().equalsIgnoreCase(userService.getCurrentUser().getEmail());
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (!configurationDAO.find().isFoodTruckRequestOn()) {
      resp.setStatus(404);
      return;
    }
    UserService userService = UserServiceFactory.getUserService();
    FoodTruckRequest.Builder builder = FoodTruckRequest.builder();
    String id = req.getParameter("id");
    if (!Strings.isNullOrEmpty(id)) {
      final long key = Long.parseLong(id);
      FoodTruckRequest existing = foodTruckRequestDAO.findById(key);
      if (!canEdit(userService, existing)) {
        resp.setStatus(403);
        return;
      }
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
    Location location = geoLocator.locate(escaper.escape(address), GeolocationGranularity.NARROW);

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
        .email(userService.getCurrentUser().getEmail())
        .userId(userService.getCurrentUser().getUserId())
        .requester(requester)
        .phone(escaper.escape(req.getParameter("phone")))
        .startTime(startTime)
        .endTime(endTime)
        .location(location)
        .build();
    // verify
    request.validate();
    long key = foodTruckRequestDAO.save(request);
    notifier.notifyNewFoodTruckRequest(FoodTruckRequest.builder(request).key(key).build());
    resp.sendRedirect("/requests/view/" + key);
  }
}
