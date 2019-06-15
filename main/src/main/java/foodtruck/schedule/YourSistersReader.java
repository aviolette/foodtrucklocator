package foodtruck.schedule;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import foodtruck.dao.TruckDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;
import foodtruck.time.Clock;

public class YourSistersReader implements StopReader {

  private static final Logger log = Logger.getLogger(YourSistersReader.class.getName());
  private final static Pattern RANGE = Pattern.compile("(\\d+):(\\d+)\\s*-\\s*(\\d+):(\\d+)");

  private final AddressExtractor extractor;
  private final TruckDAO truckDAO;
  private final ZoneId zone;
  private final Clock clock;

  @Inject
  public YourSistersReader(AddressExtractor extractor, TruckDAO truckDAO, ZoneId zone, Clock clock) {
    this.extractor = extractor;
    this.truckDAO = truckDAO;
    this.zone = zone;
    this.clock = clock;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {
    log.info("Loading your sister's tomato's calendar");
    Document parsedDoc = Jsoup.parse(document);
    ImmutableList.Builder stops = ImmutableList.builder();
    Truck truck = truckDAO.findByIdOpt("yoursisterstomato")
        .orElseThrow(() -> new RuntimeException("Your sister's tomato food truck not found"));
    for (Element item : parsedDoc.select("table")) {
      final Element span = item.previousElementSibling();
      int month = inferMonth(span.text());
      for (Element row: item.select("tr")) {
        TempTruckStop.Builder stopBuilder = TempTruckStop.builder().truckId(truck.getId()).calendarName("yoursisterstomato");
        Elements tds = row.select("td");
        Element td = tds.get(0);
        try {
          int dayOfMonth = Integer.parseInt(td.text());
          LocalDate date = LocalDate.of(2019, month, dayOfMonth);
          td = tds.get(1);
          List<Node> nodes = td.childNodes();
          Element spanElement = (Element) nodes.get(1);
          parseRange(spanElement.text(), stopBuilder, date);
          TextNode textNode = (TextNode) nodes.get(3);
          String address = textNode.text();
          extractor.parse(address, truck).stream().findFirst().ifPresent(location -> {
            stopBuilder.locationName(location);
            stops.add(stopBuilder.build());
          });
        } catch (NumberFormatException nfe) {
          continue;
        }
      }
    }
    return stops.build();
  }

  @Override
  public String getCalendar() {
    return "yoursisterstomato";
  }

  private void parseRange(String tweet, TempTruckStop.Builder builder, LocalDate date) {
    Matcher m = RANGE.matcher(tweet);
    if (m.find()) {
      int startHour = Integer.parseInt(m.group(1));
      int startMinute = Integer.parseInt(m.group(2));
      int endHour = Integer.parseInt(m.group(3));
      int endMinute = Integer.parseInt(m.group(4));
      if (startHour < 11) {
        startHour = startHour + 12;
      }
      if (startHour > endHour && endHour < 12) {
        endHour = endHour + 12;
      }
      if (startHour < endHour) {
        builder.startTime(date.atTime(startHour, startMinute).atZone(zone));
        builder.endTime(date.atTime(endHour, endMinute).atZone(zone));
        return;
      }
    }
    // fall through case for now
    builder.startTime(date.atTime(11, 0).atZone(zone));
    builder.endTime(date.atTime(14, 0).atZone(zone));
  }



  private int inferMonth(String month) {
    switch(month) {
      case "January":
        return 1;
      case "February":
        return 2;
      case "March":
        return 3;
      case "April":
        return 4;
      case "May":
        return 5;
      case "June":
        return 6;
      case "July":
        return 7;
      case "August":
        return 8;
      case "September":
        return 9;
      case "October":
        return 10;
      case "November":
        return 11;
      case "December":
        return 12;
      default:
        return clock.now8().getMonthValue();
    }
  }
}
