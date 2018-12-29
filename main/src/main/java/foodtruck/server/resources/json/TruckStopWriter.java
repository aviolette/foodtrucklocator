package foodtruck.server.resources.json;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import foodtruck.model.TruckStop;
import foodtruck.time.FriendlyDateOnlyFormat;
import foodtruck.time.HtmlDateFormatter;
import foodtruck.time.TimeOnlyFormatter;

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

  @Override
  public JSONObject forExport(TruckStop stop) {
    try {
      DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();
      return commonAttributes(stop)
          .put("startTime", dateTimeFormatter.print(stop.getStartTime()))
          .put("endTime", dateTimeFormatter.print(stop.getEndTime()));
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private JSONObject commonAttributes(TruckStop stop) {
    Period p = new Period(stop.getStartTime(), stop.getEndTime());
    try {
      return new JSONObject()
          .put("location", locationWriter.asJSON(stop.getLocation()))
          .put("id", stop.getKey())
          .put("truckId", stop.getTruck()
              .getId())
          .put("duration", period(p))
          .put("origin", stop.getOrigin())
          .put("locked", stop.isLocked())
          .put("fromBeacon", stop.isFromBeacon())
          .put("deviceId", stop.getCreatedWithDeviceId());
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public JSONObject asJSON(TruckStop stop) throws JSONException {
    return commonAttributes(stop)
        .put("startMillis", stop.getStartTime()
            .getMillis())
        .put("endMillis", stop.getEndTime()
            .getMillis())
        .put("notes", new JSONArray(stop.getNotes()))
        .put("startDate", dateFormatter.print(stop.getStartTime()))
        .put("startTime", formatter.print(stop.getStartTime()))
        .put("startTimeH", htmlDateFormatter.print(stop.getStartTime()))
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
