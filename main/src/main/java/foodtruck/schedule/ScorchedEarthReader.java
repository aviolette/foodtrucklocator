package foodtruck.schedule;

import java.time.ZonedDateTime;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import foodtruck.model.TempTruckStop;
import foodtruck.time.Clock;

import static foodtruck.schedule.SimpleCalReader.inferTruckId;

/**
 * @author aviolette
 * @since 2018-12-28
 */
public class ScorchedEarthReader implements StopReader {

  private final Clock clock;

  @Inject
  public ScorchedEarthReader(Clock clock) {
    this.clock = clock;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {
    Document parsedDoc = Jsoup.parse(document);
    ImmutableList.Builder<TempTruckStop> stops = ImmutableList.builder();
    ZonedDateTime zdt = clock.now8();
    for (Element link : parsedDoc.select("a")) {
      if (!link.attr("href").startsWith("/event")) {
        continue;
      }
      String truckId = inferTruckId(link.text());
      if (Strings.isNullOrEmpty(truckId)) {
        continue;
      }
      Element h4 = link.parent().nextElementSibling();
      String h4Text = h4.text();
      int day = Integer.parseInt(h4Text.substring(5,7));
      String time = h4Text.substring(10);
      String[] components = time.split(":");
      int minutes = 0;
      int hours;
      if (components.length == 2) {
        hours = Integer.parseInt(components[0]);
        minutes = Integer.parseInt(components[1].replace("PM",""));
        if (components[1].toLowerCase().contains("pm")) {
          hours = hours + 12;
        }
      } else {
        hours = Integer.parseInt(components[0].replace("PM", ""));
        if (components[0].toLowerCase().contains("pm")) {
          hours = hours + 12;
        }
      }

      ZonedDateTime startTime = ZonedDateTime.of(zdt.getYear(), zdt.getMonthValue(), day, hours, minutes, 0, 0, clock.zone8());
      int length = 2;
      if (hours < 16) {
        length = 4;
      }
      ZonedDateTime endTime = startTime.plusHours(length);
      stops.add(TempTruckStop.builder()
          .startTime(startTime)
          .endTime(endTime)
          .locationName("Scorched Earth Brewing")
          .truckId(truckId)
          .calendarName(getCalendar())
          .build());
    }
    return stops.build();
  }

  @Override
  public String getCalendar() {
    return "scorchedearth";
  }
}
