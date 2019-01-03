package foodtruck.schedule;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

import org.junit.Test;

import foodtruck.model.TempTruckStop;
import foodtruck.util.FakeClock;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 2018-12-30
 */
public class ScorchedEarthReaderTest extends AbstractReaderTest<ScorchedEarthReader> {

  public ScorchedEarthReaderTest() {
    super(() -> new ScorchedEarthReader(FakeClock.fixed(1546694898000L)));
  }

  @Test
  public void findStops() throws IOException {
    List<TempTruckStop> stops = execFindStop("scorchedearth.html");
    assertThat(stops).hasSize(5);
  }
}