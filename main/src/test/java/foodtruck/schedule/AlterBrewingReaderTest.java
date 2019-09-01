package foodtruck.schedule;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

import org.junit.Test;

import foodtruck.model.TempTruckStop;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 2018-12-27
 */
public class AlterBrewingReaderTest extends AbstractReaderTest<AlterBrewingReader> {

  public AlterBrewingReaderTest() {
    super(() -> new AlterBrewingReader(ZoneId.of("America/Chicago")));
  }

  @Test
  public void findStops() throws IOException {
    List<TempTruckStop> stops = execFindStop("alter.html");
    assertThat(stops).hasSize(3);
  }

  @Test
  public void findStops2() throws IOException {
    List<TempTruckStop> stops = execFindStop("alter2.html");
    assertThat(stops).hasSize(6);
  }
}