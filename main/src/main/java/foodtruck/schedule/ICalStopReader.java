package foodtruck.schedule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.model.TempTruckStop;

/**
 * @author aviolette
 * @since 10/14/18
 */
public class ICalStopReader {

  private static final Logger log = Logger.getLogger(ICalStopReader.class.getName());
  private final ICalReader reader;

  @Inject
  public ICalStopReader(ICalReader reader) {
    this.reader = reader;
  }

  public List<TempTruckStop> findStops(String document, String truck) {
    List<ICalReader.ICalEvent> events= reader.parse(document, true).stream()
        .filter(event -> event.getLocation() != null)
        .collect(Collectors.toList());

    ImmutableList.Builder<TempTruckStop> builder = ImmutableList.builder();
    events.forEach(event-> {
      deriveTruckIds(truck, event).forEach(truckId -> {
        builder.add(TempTruckStop.builder()
            .truckId(truckId)
            .calendarName("ical: " + truck)
            .startTime(event.getStart())
            .endTime(event.getEnd())
            .locationName(event.getLocation().getName())
            .build());
      });
    });
    return builder.build();
  }

  private static String categoryToTruckId(String category) {
    switch (category) {
      case "The Crave Bar":
        return "thecravebar";
      case "Toasty Taco":
        return "mytoastytaco";
      case "Toasty Cheese":
        return "mytoastycheese";
      default:
        // Probably Best Truckin' BBQ, but just return toasty cheese for now until we know true category
        log.log(Level.WARNING, "Unknown truck {0}", category);
        return "mytoastycheese";
    }
  }

  private List<String> deriveTruckIds(String truck, ICalReader.ICalEvent event) {
    if ("mytoastycheese".equals(truck) && !event.getCategories().isEmpty()) {
        return event.getCategories().stream()
            .map(ICalStopReader::categoryToTruckId)
            .collect(Collectors.toList());
    }
    return ImmutableList.of(truck);
  }
}
