package foodtruck.server.resources.json;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.TruckStop;
import foodtruck.util.FriendlyDateOnlyFormat;
import foodtruck.util.HtmlDateFormatter;
import foodtruck.util.TimeOnlyFormatter;

/**
 * @author aviolette
 * @since 7/29/13
 */
public class TruckStopWriter implements JSONWriter<TruckStop> {
  private final LocationWriter locationWriter;
  private final DateTimeFormatter formatter;
  private final DateTimeFormatter htmlDateFormatter;
  private final DateTimeFormatter dateFormatter;

  @Inject
  public TruckStopWriter(LocationWriter writer, @TimeOnlyFormatter DateTimeFormatter formatter,
      @HtmlDateFormatter DateTimeFormatter htmlDateFormatter, @FriendlyDateOnlyFormat DateTimeFormatter dateFormatter) {
    this.locationWriter = writer;
    this.formatter = formatter;
    this.htmlDateFormatter = htmlDateFormatter;
    this.dateFormatter = dateFormatter;
  }

  @Override public JSONObject asJSON(TruckStop stop) throws JSONException {
    Period p = new Period(stop.getStartTime(), stop.getEndTime());
    return new JSONObject()
        .put("location", locationWriter.asJSON(stop.getLocation()))
        .put("id", stop.getKey())
        .put("truckId", stop.getTruck().getId())
        .put("duration", period(p))
        .put("origin", stop.getOrigin())
        .put("locked", stop.isLocked())
        .put("fromBeacon", stop.isFromBeacon())
        .put("notes", new JSONArray(stop.getNotes()))
        .put("deviceId", stop.getCreatedWithDeviceId())
        .put("startDate", dateFormatter.print(stop.getStartTime()))
        .put("startTime", formatter.print(stop.getStartTime()))
        .put("startMillis", stop.getStartTime().getMillis())
        .put("startTimeH", htmlDateFormatter.print(stop.getStartTime()))
        .put("endMillis", stop.getEndTime().getMillis())
        .put("endTimeH", htmlDateFormatter.print(stop.getEndTime()))
        .put("endTime", formatter.print(stop.getEndTime()));
  }

  private String period(Period p) {
    String period = p.getHours() + ":" + (p.getMinutes() < 10 ? "0" + p.getMinutes() : p.getMinutes());
    if (p.getDays() > 0) {
      return p.getDays() + " days " + period;
    }
    return period;
  }
}
