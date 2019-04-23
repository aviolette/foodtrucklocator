package foodtruck.schedule;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;

import foodtruck.model.TempTruckStop;

import static com.google.common.truth.Truth.assertThat;

public class PerknPickleReaderTest extends AbstractReaderTest<PerknPickleReader> {

  public PerknPickleReaderTest() {
    super(() -> new PerknPickleReader(ZoneId.of("America/Chicago")));
  }

  @Test
  public void findStops() throws IOException {
    List<TempTruckStop> stops = execFindStop("perknpickle.json");
    assertThat(stops).contains(TempTruckStop.builder()
        .startTime(ZonedDateTime.of(2019, 4, 24, 17, 30, 0, 0, ZoneId.of("America/Chicago")))
        .endTime(ZonedDateTime.of(2019, 4, 24, 19, 30, 0, 0, ZoneId.of("America/Chicago")))
        .locationName("Oswego HS Spring Music")
        .truckId("perknpickle")
        .calendarName("perknpickle")
        .build());
    assertThat(stops).hasSize(86);

  }
}