package foodtruck.server.resources.json;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.TruckStop;
import foodtruck.util.TimeOnlyFormatter;

/**
 * @author aviolette
 * @since 7/29/13
 */
public class TruckStopWriter implements JSONWriter<TruckStop> {
  private final LocationWriter locationWriter;
  private final DateTimeFormatter formatter;

  @Inject
  public TruckStopWriter(LocationWriter writer, @TimeOnlyFormatter DateTimeFormatter formatter) {
    this.locationWriter = writer;
    this.formatter = formatter;
  }

  @Override public JSONObject asJSON(TruckStop stop) throws JSONException {
    return new JSONObject()
        .put("location", locationWriter.asJSON(stop.getLocation()))
        .put("truckId", stop.getTruck().getId())
        .put("fromBeacon", stop.isFromBeacon())
        .put("startTime", formatter.print(stop.getStartTime()))
        .put("startMillis", stop.getStartTime().getMillis())
        .put("endMillis", stop.getEndTime().getMillis())
        .put("endTime", formatter.print(stop.getEndTime()));
  }
}
