package foodtruck.schedule;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;

import foodtruck.model.TempTruckStop;
import foodtruck.util.FakeClock;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 2018-12-23
 */
public class PollyannaReaderTest extends AbstractReaderTest<PollyannaReader> {

  public PollyannaReaderTest() {
    super(() -> new PollyannaReader(FakeClock.fixed(1545324551000L)));
  }
  @Test
  public void findStops() throws IOException {
    List<TempTruckStop> stops = execFindStop("pollyanna.json");

    assertThat(stops).hasSize(19);
    assertThat(stops).contains(TempTruckStop.builder()
        .truckId("smokinbbqkitchn")
        .calendarName("pollyanna")
        .locationName("Pollyanna Brewing - Roselle")
        .startTime(ZonedDateTime.of(2018,12, 21, 17,30, 0, 0, CHICAGO))
        .endTime(ZonedDateTime.of(2018, 12, 21, 20, 30, 0, 0, CHICAGO))
        .build());
  }
}