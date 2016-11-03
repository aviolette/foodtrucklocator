package foodtruck.server.resources.json;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.linxup.Position;
import foodtruck.linxup.Trip;
import foodtruck.util.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 11/2/16
 */
public class TripWriter implements JSONWriter<Trip> {
  private final LocationWriter locationWriter;
  private final DateTimeFormatter dateTimeFormatter;

  @Inject
  public TripWriter(LocationWriter locationWriter, @FriendlyDateTimeFormat DateTimeFormatter dateTimeFormatter) {
    this.locationWriter = locationWriter;
    this.dateTimeFormatter = dateTimeFormatter;
  }

  @Override
  public JSONObject asJSON(Trip trip) throws JSONException {

    JSONArray positions = new JSONArray();
    for (Position pos : trip.getPositions()) {
      positions.put(new JSONObject().put("lat", pos.getLatLng()
          .getLatitude())
          .put("speed", pos.getSpeedMph())
          .put("time", dateTimeFormatter.print(pos.getDate()))
          .put("lng", pos.getLatLng()
              .getLongitude())
          .put("direction", pos.getDirection()));
    }
    return new JSONObject().put("name", trip.getName())
        .put("positions", positions)
        .put("startTime", trip.getStartTime()
            .getMillis())
        .put("endTime", trip.getEndTime()
            .getMillis())
        .put("startTimeValue", dateTimeFormatter.print(trip.getStartTime()))
        .put("endTimeValue", dateTimeFormatter.print(trip.getEndTime()))
        .put("start", locationWriter.asJSON(trip.getStart()))
        .put("end", locationWriter.asJSON(trip.getEnd()));
  }
}
