package foodtruck.server;

import java.util.Collection;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckLocationGroup;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStop;

/**
 * Writes model objects out to JSON objects.
 * @author aviolette@gmail.com
 * @since 9/14/11
 */
public class JsonWriter {
  public JSONObject writeGroup(TruckLocationGroup group) throws
      JSONException {
    return new org.codehaus.jettison.json.JSONObject()
        .put("location", writeLocation(group.getLocation()))
        .put("trucks", writeTrucks(group.getTrucks()));
  }

  public JSONArray writeTrucks(Collection<Truck> trucks) throws JSONException {
    JSONArray arr = new JSONArray();
    for (Truck truck : trucks) {
      arr.put(writeTruck(truck));
    }
    return arr;
  }

  public JSONObject writeTruck(Truck truck) throws JSONException {
    return new org.codehaus.jettison.json.JSONObject()
        .put("id", truck.getId())
        .put("description", truck.getDescription())
        .put("iconUrl", truck.getIconUrl())
        .put("twitterHandle", truck.getTwitterHandle())
        .put("name", truck.getName())
        .put("url", truck.getUrl());
  }

  public JSONObject writeLocation(Location location) throws JSONException {
    return new org.codehaus.jettison.json.JSONObject()
        .put("latitude", location.getLatitude())
        .put("longitude", location.getLongitude())
        .put("name", location.getName());
  }

  public JSONObject writeSchedule(TruckSchedule schedule) throws JSONException {
    JSONObject obj = new JSONObject()
        .put("truck", writeTruck(schedule.getTruck()))
        .put("day", schedule.getDate().toString());

    JSONArray arr = new JSONArray();
    for (TruckStop stop : schedule.getStops()) {
      JSONObject truckStop = new JSONObject()
          .put("location", writeLocation(stop.getLocation()))
          .put("startTime", stop.getStartTime().getMillis())
          .put("endTime", stop.getEndTime().getMillis());
      arr.put(truckStop);
    }
    return obj.put("stops", arr);
  }
}
