package foodtruck.schedule;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import foodtruck.model.TempTruckStop;

import static foodtruck.schedule.SimpleCalReader.inferTruckId;

/**
 * @author aviolette
 * @since 2018-12-18
 */
public class SkeletonKeyReader implements StopReader {

  private static final Logger log = Logger.getLogger(SkeletonKeyReader.class.getName());
  private final ZoneId zoneId;

  @Inject
  public SkeletonKeyReader(ZoneId zoneId) {
    this.zoneId = zoneId;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {
    log.info("Reading skeleton key's calendar");
    Document parsedDoc = Jsoup.parse(document);
    ImmutableList.Builder<TempTruckStop> stops = ImmutableList.builder();
    for (Element script : parsedDoc.select("script")) {
      String type = script.attr("type");
      if (type.equals("application/ld+json")) {
        try {
          String data = script.data().replaceAll("\n", "");
          JSONObject obj = new JSONObject(data);
          String truckId = inferTruckId(obj.getString("name"));
          if (truckId == null) {
            truckId = inferTruckId(obj.getString("description"));
            if (truckId == null) {
              continue;
            }
          }

          stops.add(TempTruckStop.builder()
              .truckId(truckId)
              .startTime(parseTime(obj.getString("startDate")))
              .endTime(parseTime(obj.getString("endDate")))
              .locationName("Skeleton Key Brewery")
              .calendarName("skeletonkey")
              .build());
        } catch (JSONException e) {
          log.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }

    return stops.build();
  }

  private ZonedDateTime parseTime(String time) {

    String[] items = time.split("-");
    int year = Integer.parseInt(items[0]);
    int month = Integer.parseInt(items[1]);
    String[] dayParts = items[2].split("T");
    int day = Integer.parseInt(dayParts[0]);
    int hour = Integer.parseInt(items[3]);
    int minute = Integer.parseInt(items[4]);

    return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zoneId);
  }
}
