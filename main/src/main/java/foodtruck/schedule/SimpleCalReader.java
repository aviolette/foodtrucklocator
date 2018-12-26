package foodtruck.schedule;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.TempTruckStop;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2018-12-11
 */
public class SimpleCalReader {

  private static final Logger log = Logger.getLogger(SimpleCalReader.class.getName());

  private final Clock clock;

  @Inject
  public SimpleCalReader(Clock clock) {
    this.clock = clock;
  }

  public List<TempTruckStop> read(JSONArray arr, String calendarName, String locationName) throws JSONException {
    log.log(Level.INFO, "Scanning calendar: {0}", calendarName);
    ImmutableList.Builder<TempTruckStop> builder = ImmutableList.builder();
    ZonedDateTime now = clock.now8();
    for (int i=0; i < arr.length(); i++) {
      JSONObject obj = arr.getJSONObject(i);
      ZonedDateTime endTime = ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(obj.getString("end")));
      if (endTime.isBefore(now)) {
        continue;
      }
      String title = obj.getString("title");
      String truckId = inferTruckId(title);
      if (truckId == null) {
        continue;
      }
      ZonedDateTime startTime = ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(obj.getString("start")));
      builder.add(TempTruckStop.builder()
          .truckId(truckId)
          .locationName(locationName)
          .startTime(startTime)
          .endTime(endTime)
          .calendarName(calendarName)
          .build());
    }
    return builder.build();
  }

  @Nullable
  static String inferTruckId(String title) {
    title = title.toLowerCase();
    if (title.contains("bop bar")) {
      return "bopbartruck";
    } else if (title.contains("puff truck")) {
      return "pufftruckpizza";
    } else if (title.contains("roaming hog")) {
      return "roaminghog";
    } else if (title.contains("toastycheese")) {
      return "mytoastycheese";
    } else if (title.contains("lucy")) {
      return "sohotruck";
    } else if (title.contains("pierogi jo")) {
      return "pierogijos";
    } else if (title.contains("doctor dogs")) {
      return "doctordogs";
    } else if (title.contains("pizza boss")) {
      return "chipizzaboss";
    } else if (title.contains("smokin bbq") || title.contains("smokin' bbq")) {
      return "smokinbbqkitchn";
    } else if (title.contains("comodita")) {
      return "ballsoflove";
    } else if (title.contains("twisted classics")) {
      return "twisted_classic";
    } else if (title.contains("babyq")) {
      return "babyqs123";
    } else if (title.contains("chicago culinary kitchen")) {
      return "chicagock";
    } else if (title.contains("cheesie")) {
      return "cheesies_truck";
    } else if (title.contains("ofrenda")) {
      return "ofrendatruck";
    } else if (title.contains("bull & balance") || title.contains("bull and balance")) {
      return "bullandbalance";
    } else if (title.contains("jd ") || title.contains("wally") ||
        title.contains("yarn night") || title.contains("matt alfano") ||
        title.contains("sip on") || title.contains("pop-up") ||
        title.contains("dj ") || title.contains("concert") ||
        title.contains("sausage making") || title.contains("open from") ||
        title.contains("music") || title.contains("santa") ||
        title.contains("welcoming") || title.contains("live") ||
        title.contains("closed") || title.contains("tour") ||
        title.contains("yoga") || title.contains("rock out") ||
        title.contains("trivia") || title.contains("beer & yoga") || !title.contains("truck")) {
      return null;
    } else {
      log.log(Level.SEVERE, "Unrecognized truck pattern: {0}", title);
    }

    return null;
  }
}