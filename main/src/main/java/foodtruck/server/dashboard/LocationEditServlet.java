package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.ReverseLookupDAO;
import foodtruck.model.Location;
import foodtruck.model.PartialLocation;
import foodtruck.model.StaticConfig;
import foodtruck.notifications.PublicEventNotificationService;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.server.resources.json.LocationReader;
import foodtruck.server.resources.json.LocationWriter;
import foodtruck.util.Urls;

import static com.google.common.base.Preconditions.checkNotNull;
import static foodtruck.server.CodedServletException.NOT_FOUND;

/**
 * @author aviolette@gmail.com
 * @since 2/13/12
 */
@Singleton
public class LocationEditServlet extends HttpServlet {
  private final LocationDAO locationDAO;
  private final LocationWriter writer;
  private final LocationReader reader;
  private final FoodTruckStopService truckStopService;
  private final PublicEventNotificationService notificationService;
  private final StaticConfig config;
  private final ReverseLookupDAO reverseLookupDAO;

  @Inject
  public LocationEditServlet(LocationDAO dao, LocationWriter writer, LocationReader reader,
      FoodTruckStopService truckStopService, PublicEventNotificationService notificationService, StaticConfig config,
      ReverseLookupDAO reverseLookupDAO) {
    this.locationDAO = dao;
    this.writer = writer;
    this.reader = reader;
    this.truckStopService = checkNotNull(truckStopService);
    this.notificationService = checkNotNull(notificationService);
    this.config = config;
    this.reverseLookupDAO = reverseLookupDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String jsp = "/WEB-INF/jsp/dashboard/locationEdit.jsp";
    final String path = Urls.stripSessionId(req.getRequestURI());
    final String keyIndex = path.substring(path.lastIndexOf("/") + 1);
    req = new GuiceHackRequestWrapper(req, jsp);
    req.setAttribute("googleApiKey", config.getGoogleJavascriptApiKey());
    Location location = locationDAO.findByIdOpt(Long.valueOf(keyIndex)).orElseThrow(NOT_FOUND);
    try {
      req.setAttribute("location", writer.writeLocation(location, 0, true));
    } catch (JSONException e) {
      throw new ServletException(e);
    }
    req.setAttribute("locationId", location.getKey());
    req.setAttribute("locationName", location.getName());
    req.setAttribute("imageUrl", location.getImageUrl());
    List<Location> aliases = locationDAO.findAliasesFor(location.getName());
    req.setAttribute("aliases", aliases);
    req.setAttribute("aliasCount", aliases.size());
    req.setAttribute("locations", locationDAO.findLocationNamesAsJson());
    req.setAttribute("nav", "locations");
    req.setAttribute("extraScripts", ImmutableList.of("/script/lib/typeahead11.js",
        "/script/typeahead-addon.js",
        "/script/lib/dropzone.js",
        "/script/dashboard-location-edit.js",
        "//maps.googleapis.com/maps/api/js?key=" + config.getGoogleJavascriptApiKey()));
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String json = new String(ByteStreams.toByteArray(req.getInputStream()));
    try {
      JSONObject jsonPayload = new JSONObject(json);
      Location location = reader.toLocation(jsonPayload);
      if (!Strings.isNullOrEmpty(location.getAlias())) {
        // if an alias is set we always want it to resolve to the alias
        location = Location.builder(location).valid(true).build();
      }
      locationDAO.save(location);
      truckStopService.updateLocationInCurrentSchedule(location);
      notificationService.updateLocationInNotifications(location);
      reverseLookupDAO.save(new PartialLocation(location.getName(), location.getLatitude(), location.getLongitude()));

      resp.setStatus(204);
    } catch (JSONException e) {
      throw new ServletException(e);
    }
  }
}
