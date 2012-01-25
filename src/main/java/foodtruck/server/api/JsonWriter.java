package foodtruck.server.api;


import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.DailySchedule;
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
  private final DateTimeFormatter timeFormatter;

  @Inject
  public JsonWriter(DateTimeZone zone) {
    timeFormatter = DateTimeFormat.forPattern("hh:mm a").withZone(zone);
  }

  public JSONObject writeGroup(TruckLocationGroup group) throws
      JSONException {
    return new org.codehaus.jettison.json.JSONObject()
        .put("location", writeLocation(group.getLocation(), 0))
        .put("trucks", writeTrucks(group.getTrucks()));
  }

  public JSONArray writeTrucks(Iterable<Truck> trucks) throws JSONException {
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
        .put("facebook", truck.getFacebook())
        .put("foursquare", truck.getFoursquareUrl())
        .put("name", truck.getName())
        .put("url", truck.getUrl());
  }

  public JSONObject writeLocation(Location location, int id) throws JSONException {
    JSONObject obj = new JSONObject()
        .put("latitude", location.getLatitude())
        .put("longitude", location.getLongitude())
        .put("name", location.getName());
    if (id != 0) {
      obj.put("id", id);
    }
    return obj;
  }

  public JSONObject writeSchedule(TruckSchedule schedule) throws JSONException {
    JSONObject obj = new JSONObject()
        .put("truck", writeTruck(schedule.getTruck()))
        .put("day", schedule.getDate().toString());

    JSONArray arr = new JSONArray();
    for (TruckStop stop : schedule.getStops()) {
      JSONObject truckStop = new JSONObject()
          .put("location", writeLocation(stop.getLocation(), 0))
          .put("id", writeKey(stop.getKey()))
          .put("startTime", timeFormatter.print(stop.getStartTime()))
          .put("endTime", timeFormatter.print(stop.getEndTime()));
      arr.put(truckStop);
    }
    return obj.put("stops", arr);
  }

  private long writeKey(Object key) {
    return ((Key)key).getId();
  }


  public JSONObject writeSchedule(DailySchedule schedule) throws JSONException {
    JSONObject payload = new JSONObject();
    Iterable<Truck> trucks =
        Iterables.transform(schedule.getStops(), new Function<TruckStop, Truck>() {
          @Override public Truck apply(TruckStop input) {
            return input.getTruck();
          }
        });
    Map<Location, Integer> locations = Maps.newHashMap();
    JSONArray locationArr = new JSONArray();
    int i = 1;
    for (TruckStop stop : schedule.getStops()) {
      locations.put(stop.getLocation(), i++);
      locationArr.put(writeLocation(stop.getLocation(), i));
    }
    JSONArray schedules = new JSONArray();
    for (TruckStop stop : schedule.getStops()) {
      JSONObject truckStop = new JSONObject()
          .put("location", locations.get(stop.getLocation()))
          .put("truckId", stop.getTruck().getId())
          .put("startTime", timeFormatter.print(stop.getStartTime()))
          .put("startMillis", stop.getStartTime().getMillis())
          .put ("endMillis", stop.getEndTime().getMillis())
          .put("endTime", timeFormatter.print(stop.getEndTime()));
      schedules.put(truckStop);
    }
    payload.put("trucks", writeTrucks(ImmutableSet.copyOf(trucks)));
    payload.put("locations", locationArr);
    payload.put("stops", schedules);
    return payload;
  }
}
