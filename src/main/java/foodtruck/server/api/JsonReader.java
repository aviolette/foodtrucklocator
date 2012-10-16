package foodtruck.server.api;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;
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


  public Location readLocation(JSONObject obj) throws JSONException {
    double lat = obj.getDouble("latitude");
    double lng = obj.getDouble("longitude");
    boolean valid = obj.getBoolean("valid");
    String name = obj.getString("name");
    long key = obj.optLong("key", 0);
    return Location.builder().lat(lat).lng(lng).name(name).key((key > 0) ? key : null)
        .url(obj.optString("url")).description(obj.optString("description")).valid(valid).build();
  }
}
