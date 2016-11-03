package foodtruck.server.resources.json;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.linxup.Position;
import foodtruck.linxup.Trip;

/**
 * @author aviolette
 * @since 11/2/16
 */
public class TripWriter implements JSONWriter<Trip> {
  private final LocationWriter locationWriter;

  @Inject
  public TripWriter(LocationWriter locationWriter) {
    this.locationWriter = locationWriter;
  }

  @Override
  public JSONObject asJSON(Trip trip) throws JSONException {

    JSONArray positions = new JSONArray();
    for (Position pos : trip.getPositions()) {
      positions.put(new JSONObject().put("latitude", pos.getLatLng()
          .getLatitude())
          .put("longitude", pos.getLatLng()
              .getLongitude())
          .put("direction", pos.getDirection()));
    }

    return new JSONObject().put("name", trip.getName())
        .put("positions", positions)
        .put("startTime", trip.getStartTime()
            .getMillis())
        .put("endTime", trip.getEndTime()
            .getMillis())
        .put("start", locationWriter.asJSON(trip.getStart()))
        .put("end", locationWriter.asJSON(trip.getEnd()));
  }
}
