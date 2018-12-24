package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

import com.google.common.io.ByteStreams;

import org.junit.Before;
import org.junit.Test;

import foodtruck.model.TempTruckStop;
import foodtruck.time.Clock;
import foodtruck.util.FakeClock;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 2018-12-23
 */
public class PollyannaReaderTest {

  private PollyannaReader reader;
  private Clock clock;

  @Before
  public void setup() {
    clock = FakeClock.fixed(1545324551000L);
    this.reader = new PollyannaReader(clock);
  }

  @Test
  public void findStops() throws IOException {
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("pollyanna.json");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);

    List<TempTruckStop> stops = reader.findStops(doc);

    assertThat(stops).hasSize(19);
    assertThat(stops).contains(TempTruckStop.builder()
        .truckId("smokinbbqkitchn")
        .calendarName("pollyanna")
        .locationName("Pollyanna Brewing - Roselle")
        .startTime(ZonedDateTime.of(2018,12,21,11,30, 0, 0, clock.zone8()))
        .endTime(ZonedDateTime.of(2018, 12,21, 14, 30, 0, 0, clock.zone8()))
        .build());
  }
}