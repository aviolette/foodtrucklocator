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
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.model.Trucks;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since 1/26/12
 */
public class JsonReader {

  private DateTimeFormatter format;
  private final Trucks trucks;
  private final Clock clock;
  private final GeoLocator geolocator;

  @Inject
  public JsonReader(Clock clock, Trucks trucks, DateTimeZone zone, GeoLocator geolocator) {
    this.trucks = trucks;
    this.clock = clock;
    this.format = DateTimeFormat.forPattern("hh:mm a").withZone(zone);
    this.geolocator = geolocator;
  }

  TruckStop read(JSONObject obj) throws JSONException {
    Truck truck = trucks.findById(obj.getString("truckId"));
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
    long key = obj.getLong("id");
    return new TruckStop(truck, startTime, endTime, location, key);
  }

  private Location parseLocation(JSONObject location) throws JSONException {
    double lat = location.getDouble("latitude");
    double lng = location.getDouble("longitude");
    String name = location.getString("name");
    return new Location(lat, lng, name);
  }


}
