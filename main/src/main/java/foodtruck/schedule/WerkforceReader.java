package foodtruck.schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import foodtruck.model.TempTruckStop;
import foodtruck.time.Clock;

import static foodtruck.schedule.SimpleCalReader.inferTruckId;
import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;

/**
 * @author aviolette
 * @since 2019-01-01
 */
public class WerkforceReader implements StopReader {

  private static final Logger log = Logger.getLogger(WerkforceReader.class.getName());

  private static final Pattern DATE = Pattern.compile("([A-Z]+)(\\d+)");
  private final Clock clock;

  @Inject
  public WerkforceReader(Clock clock) {
    this.clock = clock;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {
    ZonedDateTime now = clock.now8();
    Document parsedDoc = Jsoup.parse(document);
    ImmutableList.Builder<TempTruckStop> stops = ImmutableList.builder();
    for (Element elem : parsedDoc.select("a.image-slide-anchor")) {

      String href = elem.attr("href");
      String truckId = inferTruckId(href);
      if (Strings.isNullOrEmpty(truckId)) {
        continue;
      }
      inferTime(href, now).ifPresent(startTime -> {
        stops.add(TempTruckStop.builder()
            .startTime(startTime)
            .endTime(startTime.plusHours(4))
            .locationName("Werk Force Brewing")
            .truckId(truckId)
            .calendarName(getCalendar())
            .build());
      });

    }

    return stops.build();
  }

  private Optional<ZonedDateTime> inferTime(String href, ZonedDateTime now) {
    int index = href.lastIndexOf(".");
    String datePart = href.substring(index - 7, index);
    Matcher m = DATE.matcher(datePart);
    if (m.find()) {
      Month month = inferMonth(m.group(1));
      if (month == null) {
        log.log(Level.SEVERE, "Could not infer month {0}", m.group(1));
        return Optional.empty();
      }
      int year = now.getYear();
      if (now.getMonth() == JANUARY && month == DECEMBER) {
        year = year - 1;
      }
      LocalDate date = LocalDate.of(year, month, Integer.parseInt(m.group(2)));
      int startHour = date.getDayOfWeek() == DayOfWeek.SUNDAY ? 12 : 17;
      return Optional.of(ZonedDateTime.of(date, LocalTime.of(startHour, 0), clock.zone8()));
    }
    return Optional.empty();
  }

  @Nullable
  private Month inferMonth(String monthValue) {
    monthValue = monthValue.toUpperCase();
    switch (monthValue) {
      case "JAN":
        return JANUARY;
      case "FEB":
        return FEBRUARY;
      case "MAR":
        return MARCH;
      case "APR":
        return APRIL;
      case "MAY":
        return MAY;
      case "JUN":
        return JUNE;
      case "JUL":
      case "JULY":
        return JULY;
      case "AUG":
        return AUGUST;
      case "SEP":
      case "SEPT":
        return SEPTEMBER;
      case "OCT":
        return OCTOBER;
      case "NOV":
        return NOVEMBER;
      case "DEC":
        return DECEMBER;
      default:
        return null;
    }
  }

  @Override
  public String getCalendar() {
    return "werkforce";
  }
}
