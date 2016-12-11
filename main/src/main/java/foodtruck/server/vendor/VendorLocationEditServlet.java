package foodtruck.server.vendor;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.http.HttpStatusCodes;
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
import foodtruck.mail.SystemNotificationService;
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
import foodtruck.util.FormDataMassager;

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
  private final SystemNotificationService notificationService;

  @Inject
  public VendorLocationEditServlet(TruckDAO dao, UserService userService, Provider<SessionUser> sessionUserProvider,
      LocationDAO locationDAO, StaticConfig config, LocationWriter locationWriter, LocationReader reader,
      @HtmlDateFormatter DateTimeFormatter formatter, FoodTruckStopService stopService,
      SystemNotificationService notificationService) {
    super(dao, userService, sessionUserProvider);
    this.locationDAO = locationDAO;
    this.config = config;
    this.locationWriter = locationWriter;
    this.reader = reader;
    this.formatter = formatter;
    this.stopService = stopService;
    this.notificationService = notificationService;
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
    String locationId = req.getParameter("locationId");
    Location location = locationDAO.findById(Long.parseLong(locationId));
    if (location == null) {
      resp.sendError(404, "Could not find location with ID: " + locationId);
      return;
    }
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
  protected void dispatchPut(HttpServletRequest req, HttpServletResponse resp, Truck truck,
      Principal principal) throws IOException {
    final String json = new String(ByteStreams.toByteArray(req.getInputStream()));
    try {
      // TODO: use FormDataMassager in VendorSettingsServlet
      JSONObject jsonPayload = new JSONObject(json);
      Location location = reader.toLocation(jsonPayload);
      Location existing = locationDAO.findById((Long) location.getKey());

      if (existing.isValid() && !principal.getName()
          .equals(existing.getCreatedBy())) {
        log.log(Level.WARNING, "User {0} cannot edit {1}", new Object[]{principal.getName(), existing});
        resp.sendError(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED, "You can only edit this location if you created it");
        return;
      }
      location = Location.builder(existing)
          .autocomplete(true)
          .valid(true)
          .alexaProvided(true)
          .createdBy(principal.getName())
          .description(FormDataMassager.escape(jsonPayload.optString("description")))
          .lat(jsonPayload.getDouble("latitude"))
          .lng(jsonPayload.getDouble("longitude"))
          .url(FormDataMassager.escapeUrl(jsonPayload.optString("url")))
          .twitterHandle(FormDataMassager.escape(jsonPayload.getString("twitterHandle")))
          .facebookUri(FormDataMassager.escape(jsonPayload.optString("facebook")))
          .email(FormDataMassager.escape(jsonPayload.optString("email")))
          .phoneNumber(FormDataMassager.normalizePhone(jsonPayload.optString("phone")))
          .hasBooze(jsonPayload.optBoolean("hasBooze"))
          .build();
      locationDAO.save(location);
      notificationService.systemNotifyLocationAdded(location, principal.getName());
      resp.setStatus(204);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
