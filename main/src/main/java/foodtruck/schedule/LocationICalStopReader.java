package foodtruck.schedule;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import foodtruck.model.TempTruckStop;

import static foodtruck.schedule.SimpleCalReader.inferTruckId;

/**
 * @author aviolette
 * @since 2019-01-03
 */
public class LocationICalStopReader {

  private final ICalReader reader;

  @Inject
  public LocationICalStopReader(ICalReader reader) {
    this.reader = reader;
  }

  public List<TempTruckStop> findStops(String document, String defaultLocation, String calendarName) {
    return reader.parse(document).stream()
        .map(event -> {
          String truckId = inferTruckId(event.getSummary());
          if (Strings.isNullOrEmpty(truckId)) {
            truckId = inferTruckId(event.getDescription());
            if (Strings.isNullOrEmpty(truckId)) {
              return null;
            }
          }
          String location = event.getLocation() == null ? null : event.getLocation().getName();
          location = MoreObjects.firstNonNull(defaultLocation, location);
          if (location == null) {
            return null;
          }
          return TempTruckStop.builder()
              .truckId(truckId)
              .startTime(event.getStart())
              .endTime(event.getEnd())
              .locationName(location)
              .calendarName(calendarName)
              .build();
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
