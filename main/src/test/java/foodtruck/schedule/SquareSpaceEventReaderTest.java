package foodtruck.schedule;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;

import foodtruck.model.TempTruckStop;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 2018-12-27
 */
public class SquareSpaceEventReaderTest extends AbstractReaderTest<SquareSpaceEventReader> {

  public SquareSpaceEventReaderTest() {
    super(() -> new SquareSpaceEventReader(CHICAGO));
  }

  @Test
  public void findStops() throws IOException {
    List<TempTruckStop> stops = execFindStop("plankroad.html");
    assertThat(stops).hasSize(12);
    assertThat(stops).contains(TempTruckStop.builder()
        .truckId("chuckswoodfired")
        .startTime(ZonedDateTime.of(2018, 12, 31, 3, 0, 0, 0, CHICAGO))
        .endTime(ZonedDateTime.of(2018, 12, 31, 20, 0, 0, 0, CHICAGO))
        .locationName("Plank Road Tap Room")
        .calendarName("plankroadtaproom")
        .build());
  }

  @Test
  public void findStops2() throws IOException {
    List<TempTruckStop> stops = execFindStop("temperance.html");
    assertThat(stops).hasSize(2);
    System.out.println(stops);
    assertThat(stops).contains(TempTruckStop.builder()
        .truckId("threeleggedtaco")
        .startTime(ZonedDateTime.of(2018, 12, 8, 11, 0, 0, 0, CHICAGO))
        .endTime(ZonedDateTime.of(2018, 12, 8, 17, 0, 0, 0, CHICAGO))
        .locationName("Temperance Beer Co.")
        .calendarName("squarespace: Temperance Beer Co.")
        .build());

  }
}