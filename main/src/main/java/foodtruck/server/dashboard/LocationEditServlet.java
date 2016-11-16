package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.notifications.EventNotificationService;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.server.resources.json.LocationReader;
import foodtruck.server.resources.json.LocationWriter;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Urls;

import static com.google.common.base.Preconditions.checkNotNull;

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
  private final EventNotificationService notificationService;
  private final StaticConfig config;

  @Inject
  public LocationEditServlet(LocationDAO dao, LocationWriter writer, LocationReader reader,
      FoodTruckStopService truckStopService, EventNotificationService notificationService, StaticConfig config) {
    this.locationDAO = dao;
    this.writer = writer;
    this.reader = reader;
    this.truckStopService = checkNotNull(truckStopService);
    this.notificationService = checkNotNull(notificationService);
    this.config = config;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String jsp = "/WEB-INF/jsp/dashboard/locationEdit.jsp";
    final String path = Urls.stripSessionId(req.getRequestURI());
    final String keyIndex = path.substring(path.lastIndexOf("/") + 1);
    req = new GuiceHackRequestWrapper(req, jsp);
    req.setAttribute("googleApiKey", config.getGoogleJavascriptApiKey());
    Location location = locationDAO.findById(Long.valueOf(keyIndex));
    if (location != null) {
      try {
        req.setAttribute("location", writer.writeLocation(location, 0, true));
        req.setAttribute("locationId", location.getKey());
        req.setAttribute("aliases", locationDAO.findAliasesFor(location.getName()));
      } catch (JSONException e) {
        throw Throwables.propagate(e);
      }
    }
    List<String> locationNames = ImmutableList.copyOf(
        Iterables.transform(locationDAO.findAutocompleteLocations(), Location.TO_NAME));
    req.setAttribute("locations", new JSONArray(locationNames).toString());
    req.setAttribute("nav", "locations");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String json = new String(ByteStreams.toByteArray(req.getInputStream()));
    try {
      JSONObject jsonPayload = new JSONObject(json);
      Location location = reader.toLocation(jsonPayload);
      locationDAO.save(location);
      truckStopService.updateLocationInCurrentSchedule(location);
      notificationService.updateLocationInNotifications(location);
      resp.setStatus(204);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
