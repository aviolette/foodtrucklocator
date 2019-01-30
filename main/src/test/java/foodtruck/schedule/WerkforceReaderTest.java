package foodtruck.schedule;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import foodtruck.model.TempTruckStop;
import foodtruck.util.FakeClock;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 2019-01-01
 */
public class WerkforceReaderTest extends AbstractReaderTest<WerkforceReader> {

  public WerkforceReaderTest() {
    super(() -> new WerkforceReader(FakeClock.fixed(1546349108000L)));
  }

  @Test
  public void findStops() throws IOException {
    List<TempTruckStop> stops = execFindStop("werkforce.html");
    assertThat(stops).hasSize(12);
  }

  @Test
  public void findStops2() throws IOException {
    List<TempTruckStop> stops = execFindStop("werkforce2.html");
    assertThat(stops).hasSize(11);
  }
}