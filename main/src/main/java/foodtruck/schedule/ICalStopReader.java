package foodtruck.schedule;

import java.io.IOException;
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
    return reader.parse(document).stream()
        .map(event -> TempTruckStop.builder()
            .truckId(truck)
            .calendarName("ical: " + truck)
            .startTime(event.getStart())
            .endTime(event.getEnd())
            .locationName(event.getLocation().getName())
            .build())
        .collect(Collectors.toList());
  }
}
