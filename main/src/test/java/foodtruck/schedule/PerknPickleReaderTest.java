package foodtruck.schedule;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.TempTruckStop;

import static com.google.common.truth.Truth.assertThat;

public class PerknPickleReaderTest {

  @Mock GeoLocator geoLocator;

  @Ignore
  @Test
  public void findStops() throws IOException {
/*
    List<TempTruckStop> stops = execFindStop("perknpickle.json");
    assertThat(stops).contains(TempTruckStop.builder()
        .startTime(ZonedDateTime.of(2019, 4, 24, 17, 30, 0, 0, ZoneId.of("America/Chicago")))
        .endTime(ZonedDateTime.of(2019, 4, 24, 19, 30, 0, 0, ZoneId.of("America/Chicago")))
        .locationName("Oswego HS Spring Music")
        .truckId("perknpickle")
        .calendarName("perknpickle")
        .build());
    assertThat(stops).hasSize(86);
*/
  }
}