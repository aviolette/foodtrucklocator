package foodtruck.server.vendor;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.server.resources.json.LocationReader;
import foodtruck.server.resources.json.LocationWriter;
import foodtruck.time.HtmlDateFormatter;

/**
 * @author aviolette
 * @since 12/8/16
 */
@Singleton
public class VendorLocationEditServlet extends VendorServletSupport {
  private static final Logger log = Logger.getLogger(VendorLocationEditServlet.class.getName());

  private static final String JSP = "/WEB-INF/jsp/vendor/locationEdit.jsp";
  private final StaticConfig config;
  private final LocationWriter locationWriter;
  private final LocationDAO locationDAO;
  private final LocationReader reader;
  private final DateTimeFormatter formatter;
  private final FoodTruckStopService stopService;

  @Inject
  public VendorLocationEditServlet(TruckDAO dao, UserService userService, Provider<SessionUser> sessionUserProvider,
      LocationDAO locationDAO, StaticConfig config, LocationWriter locationWriter, LocationReader reader,
      @HtmlDateFormatter DateTimeFormatter formatter, FoodTruckStopService stopService) {
    super(dao, userService, sessionUserProvider);
    this.locationDAO = locationDAO;
    this.config = config;
    this.locationWriter = locationWriter;
    this.reader = reader;
    this.formatter = formatter;
    this.stopService = stopService;
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Truck truck) throws ServletException, IOException {
    String locationId = req.getRequestURI()
        .substring(18);
    Location location = locationDAO.findById(Long.parseLong(locationId.substring(0, locationId.lastIndexOf('/'))));
    if (location == null) {
      resp.sendError(404);
      return;
    }
    req = new GuiceHackRequestWrapper(req, JSP);
    req.setAttribute("locationId", location.getKey());
    req.setAttribute("googleApiKey", config.getGoogleJavascriptApiKey());
    String startTime = req.getParameter("startTime"), endTime = req.getParameter("endTime");
    if (!Strings.isNullOrEmpty(startTime) && !Strings.isNullOrEmpty(endTime)) {
      req.setAttribute("startTime", startTime);
      req.setAttribute("endTime", endTime);
    }
    try {
      req.setAttribute("location", locationWriter.asJSON(location));
    } catch (JSONException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      resp.sendError(500);
      return;
    }
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }

  @Override
  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, String truckId, Principal principal) throws IOException {
    DateTime startTime = formatter.parseDateTime(req.getParameter("startTime")), endTime = formatter.parseDateTime(
        req.getParameter("endTime"));
    Truck truck = truckDAO.findById(truckId);
    Location location = locationDAO.findById(Long.parseLong(req.getParameter("locationId")));
    // TODO: what if locaiton not found
    // TODO: what if startTime poorly formatted
    // TODO: what if endtime poorly formatted

    TruckStop stop = TruckStop.builder()
        .startTime(startTime)
        .endTime(endTime)
        .truck(truck)
        .origin(StopOrigin.MANUAL)
        .location(location)
        .build();

    stopService.update(stop, principal.getName());
  }

  @Override
  protected void dispatchPut(HttpServletRequest req, HttpServletResponse resp, Truck truck) throws IOException {
    final String json = new String(ByteStreams.toByteArray(req.getInputStream()));
    try {
      JSONObject jsonPayload = new JSONObject(json);
      Location location = reader.toLocation(jsonPayload);
      Location existing = locationDAO.findById((Long) location.getKey());
      location = Location.builder(existing)
          .autocomplete(true)
          .valid(true)
          .description(jsonPayload.optString("description"))
          .lat(jsonPayload.getDouble("latitude"))
          .lng(jsonPayload.getDouble("longitude"))
          .url(jsonPayload.optString("url"))
          .twitterHandle(jsonPayload.getString("twitterHandle"))
          .facebookUri(jsonPayload.optString("facebook"))
          .email(jsonPayload.optString("email"))
          .phoneNumber(jsonPayload.optString("phone"))
          .hasBooze(jsonPayload.optBoolean("hasBooze"))
          .build();
      locationDAO.save(location);
      resp.setStatus(204);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
