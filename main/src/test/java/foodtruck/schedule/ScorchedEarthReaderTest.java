package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.zip.CheckedInputStream;

import com.google.common.io.ByteStreams;

import org.junit.Test;

import foodtruck.model.TempTruckStop;
import foodtruck.util.FakeClock;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.AbstractReaderTest.CHICAGO;

/**
 * @author aviolette
 * @since 2018-12-30
 */
public class ScorchedEarthReaderTest {

  @Test
  public void findStops() throws IOException {
    ZonedDateTime now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1546694898000L), CHICAGO);
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("scorchedearth.html");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);

    List<TempTruckStop> stops = new ScorchedEarthReader().findStops(doc, now);
    assertThat(stops).hasSize(5);
    assertThat(stops).contains(TempTruckStop.builder()
        .truckId("dukesbluesnbbq")
        .calendarName("Scorched Earth Brewing")
        .locationName("Scorched Earth Brewing")
        .startTime(ZonedDateTime.of(2019, 1, 12, 17, 30, 0, 0, CHICAGO))
        .endTime(ZonedDateTime.of(2019, 1, 12, 19, 30, 0, 0, CHICAGO))
        .build());
  }
}