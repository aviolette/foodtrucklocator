package foodtruck.server.api;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since 1/26/12
 */
public class JsonReader {
  private DateTimeFormatter format;
  private final Clock clock;
  private final GeoLocator geolocator;
  private final TruckDAO truckDAO;

  @Inject
  public JsonReader(Clock clock, TruckDAO trucks, DateTimeZone zone, GeoLocator geolocator) {
    this.truckDAO = trucks;
    this.clock = clock;
    this.format = DateTimeFormat.forPattern("hh:mm a").withZone(zone);
    this.geolocator = geolocator;
  }

  public TruckStop readTruckStop(JSONObject obj) throws JSONException {
    Truck truck = truckDAO.findById(obj.getString("truckId"));
    checkNotNull(truck);
    LocalDate today = clock.currentDay();
    DateTime startTime = format.parseDateTime(obj.getString("startTime"))
        .withDate(today.getYear(), today.getMonthOfYear(), today.getDayOfMonth());
    DateTime endTime = format.parseDateTime(obj.getString("endTime"))
        .withDate(today.getYear(), today.getMonthOfYear(), today.getDayOfMonth());
    final JSONObject loc = obj.optJSONObject("location");
    Location location;
    if (loc == null) {
      location = geolocator.locate(obj.getString("locationName"), GeolocationGranularity.NARROW);
      checkNotNull(location, "Location couldn't be resolved");
    } else {
      location = parseLocation(loc);
      checkNotNull(location, "Location is unparsable");
    }
    checkState(location.isResolved(), "Location is not resolved");
    long key = obj.optLong("id", 0);
    return new TruckStop(truck, startTime, endTime, location, (key > 0) ? key : null);
  }

  public Location readLocation(JSONObject obj) throws JSONException {
    double lat = obj.getDouble("latitude");
    double lng = obj.getDouble("longitude");
    boolean valid = obj.getBoolean("valid");
    String name = obj.getString("name");
    long key = obj.optLong("key", 0);
    return Location.builder().lat(lat).lng(lng).name(name).key((key > 0) ? key : null)
        .valid(valid).build();
  }


  private Location parseLocation(JSONObject location) throws JSONException {
    double lat = location.getDouble("latitude");
    double lng = location.getDouble("longitude");
    String name = location.getString("name");
    return Location.builder().lat(lat).lng(lng).name(name).build();
  }
}
