package foodtruck.schedule;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import foodtruck.model.TempTruckStop;

import static foodtruck.schedule.SimpleCalReader.inferTruckId;

/**
 * @author aviolette
 * @since 2018-12-27
 */
public class AlterBrewingReader implements StopReader {

  private static final Logger log = Logger.getLogger(AlterBrewingReader.class.getName());
  private final ZoneId zone;

  @Inject
  public AlterBrewingReader(ZoneId zoneId) {
    zone = zoneId;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {
    log.info("Reading alter brewing's calendar");
    Document parsedDoc = Jsoup.parse(document);
    ImmutableList.Builder<TempTruckStop> stops = ImmutableList.builder();
    for (Element script : parsedDoc.select("script")) {
      String type = script.attr("type");
      if (type.equals("application/ld+json")) {
        try {
          JSONArray arr = new JSONArray(script.data());
          for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String truckId = inferTruckId(obj.getString("name"));
            if (Strings.isNullOrEmpty(truckId)) {
              truckId = inferTruckId(obj.getString("description"));
              if (Strings.isNullOrEmpty(truckId)) {
                continue;
              }
              continue;
            }
            stops.add(TempTruckStop.builder()
                .calendarName(getCalendar())
                .locationName("Alter Brewing")
                .startTime(parseTime(obj.getString("startDate")))
                .endTime(parseTime(obj.getString("endDate")))
                .truckId(truckId)
                .build());
          }
          break;
        } catch (JSONException e) {
          if (!e.getMessage().startsWith("A JSONArray text must start with '['")) {
            log.log(Level.SEVERE, e.getMessage(), e);
            break;
          }
        }
      }
    }
    return stops.build();
  }

  private ZonedDateTime parseTime(String date) {
    OffsetDateTime offsetDateTime = OffsetDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(date));
    return ZonedDateTime.ofInstant(offsetDateTime.toInstant(), zone);
  }

  @Override
  public String getCalendar() {
    return "Alter Brewing";
  }
}
