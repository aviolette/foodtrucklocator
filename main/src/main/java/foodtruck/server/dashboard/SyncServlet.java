package foodtruck.server.dashboard;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.server.resources.json.LocationReader;
import foodtruck.server.resources.json.TruckReader;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 4/25/14
 */
@Singleton
public class SyncServlet extends HttpServlet {
  private final StaticConfig staticConfig;
  private final TruckReader truckReader;
  private final TruckDAO truckDAO;
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;
  private final LocationReader locationReader;
  private final LocationDAO locationDAO;

  @Inject
  public SyncServlet(StaticConfig staticConfig, TruckReader truckReader, TruckDAO truckDAO, TruckStopDAO truckStopDAO,
      Clock clock, LocationReader locationReader, LocationDAO locationDAO) {
    this.truckReader = truckReader;
    this.truckDAO = truckDAO;
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
    this.locationReader = locationReader;
    this.locationDAO = locationDAO;
    this.staticConfig = staticConfig;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("nav", "sync");
    req.setAttribute("syncEnabled", !Strings.isNullOrEmpty(staticConfig.getSyncUrl()));
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/sync.jsp").forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    boolean syncTrucks = "on".equals(req.getParameter("trucks")),
      syncSchedule = "on".equals(req.getParameter("schedule"));

    Client c = Client.create();
    if (syncTrucks) {
      truckDAO.deleteAll();
      JSONArray arr = c.resource(staticConfig.getSyncUrl() + "/services/trucks?appKey="+staticConfig.getSyncAppKey())
          .accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);
      for (int i=0; i < arr.length(); i++) {
        try {
          Truck truck = truckReader.asJSON(arr.getJSONObject(i));
          truckDAO.save(Truck.builder(truck).useTwittalyzer(true).build());
        } catch (JSONException e) {
          throw new ServletException(e);
        }
      }
    }
    if (syncSchedule) {
      final DateTime from = clock.now().withHourOfDay(0).withMinuteOfHour(0);
      truckStopDAO.deleteAfter(from);
      JSONObject dailySchedule = c.resource(staticConfig.getSyncUrl() + "/services/daily_schedule?appKey="+staticConfig.getSyncAppKey())
          .accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

      try {
        JSONArray locations = dailySchedule.getJSONArray("locations");
        Location loc[] = new Location[locations.length()];
        for (int i=0; i < locations.length(); i++) {
          Location location = locationReader.toLocation(locations.getJSONObject(i));
          Location existing = locationDAO.findByAddress(location.getName());
          if (existing != null) {
            locationDAO.delete((Long) location.getKey());
          }
          locationDAO.save(location);
          loc[i] = location;
        }
        JSONArray schedules = dailySchedule.getJSONArray("stops");
        for (int i=0; i < schedules.length(); i++) {
          JSONObject stopJSON = schedules.getJSONObject(i);
          Location location = loc[stopJSON.getInt("location") - 1];
          TruckStop stop = TruckStop.builder()
              .endTime(new DateTime(stopJSON.getLong("endMillis")))
              .startTime(new DateTime(stopJSON.getLong("startMillis")))
              .truck(truckDAO.findById(stopJSON.getString("truckId")))
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
}
