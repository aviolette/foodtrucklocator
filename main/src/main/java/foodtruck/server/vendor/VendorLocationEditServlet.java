package foodtruck.server.vendor;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LocationDAO;
import foodtruck.mail.SystemNotificationService;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.server.CodedServletException;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.server.resources.json.LocationReader;
import foodtruck.server.resources.json.LocationWriter;
import foodtruck.time.HtmlDateFormatter;
import foodtruck.util.FormDataMassager;

import static foodtruck.server.vendor.VendorPageFilter.PRINCIPAL;

/**
 * @author aviolette
 * @since 12/8/16
 */
@Singleton
public class VendorLocationEditServlet extends HttpServlet {

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
  public VendorLocationEditServlet(LocationDAO locationDAO, StaticConfig config,
      LocationWriter locationWriter, LocationReader reader,
      @HtmlDateFormatter DateTimeFormatter formatter, FoodTruckStopService stopService,
      SystemNotificationService notificationService) {
    this.locationDAO = locationDAO;
    this.config = config;
    this.locationWriter = locationWriter;
    this.reader = reader;
    this.formatter = formatter;
    this.stopService = stopService;
    this.notificationService = notificationService;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String locationId = req.getRequestURI().substring(18);
    Location location = locationDAO.findByIdOpt(
        Long.parseLong(locationId.substring(0, locationId.lastIndexOf('/'))))
        .orElseThrow(() -> new CodedServletException(404, "Location not found: " + locationId));
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
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    DateTime startTime = formatter.parseDateTime(
        req.getParameter("startTime")), endTime = formatter.parseDateTime(
        req.getParameter("endTime"));
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    String locationId = req.getParameter("locationId");
    Location location = locationDAO.findByIdOpt(
        Long.parseLong(locationId.substring(0, locationId.lastIndexOf('/'))))
        .orElseThrow(() -> new CodedServletException(404, "Location not found: " + locationId));
    // TODO: what if startTime poorly formatted
    // TODO: what if endtime poorly formatted

    TruckStop stop = TruckStop.builder()
        .startTime(startTime)
        .endTime(endTime)
        .truck(truck)
        .origin(StopOrigin.MANUAL)
        .location(location)
        .build();
    Principal principal = (Principal) req.getAttribute(PRINCIPAL);
    stopService.update(stop, principal.getName());
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    final String json = new String(ByteStreams.toByteArray(req.getInputStream()));
    Principal principal = (Principal) req.getAttribute(PRINCIPAL);
    try {
      // TODO: use FormDataMassager in VendorSettingsServlet
      JSONObject jsonPayload = new JSONObject(json);
      Location location = reader.toLocation(jsonPayload);
      Location existing = locationDAO.findByIdOpt((Long) location.getKey())
          .orElseThrow(() -> new CodedServletException(404, "Location not found"));

      if (existing.isValid() && !principal.getName()
          .equals(existing.getCreatedBy())) {
        log.log(Level.WARNING, "User {0} cannot edit {1}",
            new Object[]{principal.getName(), existing});
        resp.sendError(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED,
            "You can only edit this location if you created it");
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
      throw new ServletException(e);
    }
  }
}
