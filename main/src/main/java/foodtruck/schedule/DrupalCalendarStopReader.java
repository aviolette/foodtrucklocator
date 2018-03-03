package foodtruck.schedule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

/**
 * @author aviolette
 * @since 2/18/18
 */
public class DrupalCalendarStopReader {

  private static final Logger log = Logger.getLogger(DrupalCalendarStopReader.class.getName());

  private static DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd");
  private static DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("hh:mma");
  private final GeoLocator geoLocator;
  private final DateTimeZone zone;

  @Inject
  public DrupalCalendarStopReader(GeoLocator geoLocator, DateTimeZone zone) {
    this.geoLocator = geoLocator;
    this.zone = zone;
  }

  public List<TruckStop> read(String document, Truck truck) throws WebApplicationException {
    ImmutableList.Builder<TruckStop> truckStopBuilder = ImmutableList.builder();
    Document dom = Jsoup.parse(document);
    Elements items = dom.select("td.single-day");
    for (Element element : items) {
      Elements contents = element.select(".contents");
      String dateData = element.attr("data-date");
      for (Element calendarEntry : contents) {
        TruckStop.Builder builder = TruckStop.builder()
            .truck(truck);
        if (Strings.isNullOrEmpty(dateData)) {
          continue;
        }
        String locationName = calendarEntry.select(".views-field-title").text();
        if (Strings.isNullOrEmpty(locationName)) {
          continue;
        }
        Location location = geoLocator.locate(locationName, GeolocationGranularity.NARROW);
        if (location == null || !location.isResolved()) {
          log.log(Level.WARNING, "Location not resolved: {0}", locationName);
          continue;
        }
        builder.location(location);
        String timeText = calendarEntry.select(".date-display-start").text();
        LocalDate ld = formatter.parseLocalDate(dateData);
        LocalTime lt = hourFormatter.parseLocalTime(timeText);
        DateTime startDate = ld.toDateTime(lt, zone);
        builder.startTime(startDate);
        timeText = calendarEntry.select(".date-display-end").text();
        lt = hourFormatter.parseLocalTime(timeText);
        DateTime endDate = ld.toDateTime(lt, zone);
        if (startDate.isAfter(endDate)) {
          endDate = endDate.plusDays(1);
        }
        builder.endTime(endDate).origin(StopOrigin.VENDORCAL);
        truckStopBuilder.add(builder.build());
      }
   }
    return truckStopBuilder.build();
  }
}
