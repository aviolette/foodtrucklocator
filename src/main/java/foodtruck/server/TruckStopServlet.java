package foodtruck.server;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckLocationGroup;
import foodtruck.truckstops.FoodTruckStopService;

/**
 * @author aviolette@gmail.com
 * @since 9/6/11
 */
@Singleton
public class TruckStopServlet extends HttpServlet {
  private final FoodTruckStopService foodTruckService;
  private final DateTimeZone zone;
  private final DateTimeFormatter timeFormatter;

  @Inject
  public TruckStopServlet(FoodTruckStopService service, DateTimeZone zone) {
    this.foodTruckService = service;
    this.zone = zone;
    this.timeFormatter = DateTimeFormat.forPattern("YYYYMMdd-HHmm").withZone(zone);

  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String timeRequest = req.getParameter("time");
    DateTime dateTime = null;
    if (!Strings.isNullOrEmpty(timeRequest)) {
      try {
        dateTime = timeFormatter.parseDateTime(timeRequest);
      } catch (IllegalArgumentException ignored) {
      }
    }
    if (dateTime == null) {
      dateTime = new DateTime(zone);
    }
    JSONArray arr = new JSONArray();
    Set<TruckLocationGroup> foodTruckGroups = foodTruckService.findFoodTruckGroups(dateTime);
    try {

      for (TruckLocationGroup group : foodTruckGroups) {
        if (group.getLocation() != null) {
          arr.put(writeGroup(group));
        }
      }
      resp.setHeader("Content-Type", "application/json");
      resp.getWriter().println(arr.toString());
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private JSONObject writeGroup(TruckLocationGroup group) throws JSONException {
    return new JSONObject()
        .put("location", writeLocation(group.getLocation()))
        .put("trucks", writeTrucks(group.getTrucks()));
  }

  private JSONArray writeTrucks(Collection<Truck> trucks) throws JSONException {
    JSONArray arr = new JSONArray();
    for (Truck truck : trucks) {
      arr.put(writeTruck(truck));
    }
    return arr;
  }

  private JSONObject writeTruck(Truck truck) throws JSONException {
    return new JSONObject()
        .put("id", truck.getId())
        .put("description", truck.getDescription())
        .put("iconUrl", truck.getIconUrl())
        .put("twitterHandle", truck.getTwitterHandle())
        .put("name", truck.getName())
        .put("url", truck.getUrl());
  }

  private JSONObject writeLocation(Location location) throws JSONException {
    return new JSONObject()
        .put("latitude", location.getLatitude())
        .put("longitude", location.getLongitude())
        .put("name", location.getName());
  }
}
