package foodtruck.server.dashboard;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.net.UrlResource;
import foodtruck.server.resources.json.LocationReader;
import foodtruck.server.resources.json.TruckReader;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 4/25/14
 */
@Singleton
public class SyncServlet extends HttpServlet {

  private final TruckReader truckReader;
  private final TruckDAO truckDAO;
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;
  private final LocationReader locationReader;
  private final LocationDAO locationDAO;
  private final UrlResource urls;

  @Inject
  public SyncServlet(TruckReader truckReader, TruckDAO truckDAO, TruckStopDAO truckStopDAO,
      Clock clock, LocationReader locationReader, LocationDAO locationDAO, UrlResource urls) {
    this.truckReader = truckReader;
    this.truckDAO = truckDAO;
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
    this.locationReader = locationReader;
    this.locationDAO = locationDAO;
    this.urls = urls;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setAttribute("nav", "sync");
    req.setAttribute("syncEnabled", !Strings.isNullOrEmpty(getSyncUrl()));
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/sync.jsp")
        .forward(req, resp);
  }

  private String getSyncUrl() {
    return System.getenv().get("FOODTRUCK_SYNC_URL");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    boolean syncTrucks = "on".equals(req.getParameter("trucks")), syncSchedule = "on".equals(
        req.getParameter("schedule"));

    if (syncTrucks) {
      truckDAO.deleteAll();
      JSONArray arr = urls.getAsArray(
          getSyncUrl() + "/services/trucks?appKey=" + getSyncAppKey());
      for (int i = 0; i < arr.length(); i++) {
        try {
          Truck truck = truckReader.asJSON(arr.getJSONObject(i));
          truckDAO.save(Truck.builder(truck)
              .useTwittalyzer(true)
              .build());
        } catch (JSONException e) {
          throw new ServletException(e);
        }
      }
    }
    if (syncSchedule) {
      final DateTime from = clock.now()
          .withHourOfDay(0)
          .withMinuteOfHour(0);
      truckStopDAO.deleteAfter(from);
      JSONObject dailySchedule = urls.getAsJson(
          getSyncUrl() + "/services/daily_schedule?appKey=" + getSyncAppKey());

      try {
        JSONArray locations = dailySchedule.getJSONArray("locations");
        Location[] loc = new Location[locations.length()];
        for (int i = 0; i < locations.length(); i++) {
          Location location = locationReader.toLocation(locations.getJSONObject(i));
          locationDAO.findByName(location.getName())
              .ifPresent(existing -> locationDAO.delete((Long) location.getKey()));
          locationDAO.save(location);
          loc[i] = location;
        }
        JSONArray schedules = dailySchedule.getJSONArray("stops");
        for (int i = 0; i < schedules.length(); i++) {
          JSONObject stopJSON = schedules.getJSONObject(i);
          Location location = loc[stopJSON.getInt("location") - 1];
          String truckId = stopJSON.getString("truckId");
          TruckStop stop = TruckStop.builder()
              .endTime(new DateTime(stopJSON.getLong("endMillis")))
              .startTime(new DateTime(stopJSON.getLong("startMillis")))
              .truck(truckDAO.findByIdOpt(truckId)
                  .orElse(Truck.builder()
                      .id(truckId)
                      .build()))
              .location(location)
              .build();
          truckStopDAO.save(stop);
        }
      } catch (JSONException e) {
        throw new ServletException(e);
      }
    }
    resp.sendRedirect("/admin/trucks");
  }

  private String getSyncAppKey() {
    return System.getenv().get("FOODTRUCK_SYNC_KEY");
  }
}
