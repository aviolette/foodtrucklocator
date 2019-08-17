package foodtruck.schedule;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.TruckDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.ModelTestHelper.clarkAndMonroe;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThreeLeggedReaderTest extends AbstractReaderTest<ThreeLeggedReader> {

  @Mock public CalendarAddressExtractor extractor;
  @Mock public TruckDAO truckDAO;

  @Before
  public void setup() {
    this.supplier = () -> new ThreeLeggedReader(extractor, truckDAO, ModelTestHelper.ZONE);
  }

  @Test
  public void findStops() throws IOException {
    Truck truck = Truck.builder().id("threeleggedtaco").name("Three Legged Taco").build();
    when(truckDAO.findByIdOpt("threeleggedtaco")).thenReturn(Optional.of(truck));
    when(extractor.parse(any(), any())).thenReturn(Optional.empty());
    when(extractor.parse("Mikerphone Brewing", truck)).thenReturn(Optional.of(clarkAndMonroe()));
    List<TempTruckStop> stops = execFindStop("threelegged.json");
    assertThat(stops).hasSize(1);
    assertThat(stops).contains(TempTruckStop.builder()
        .truckId("threeleggedtaco")
        .startTime(ZonedDateTime.of(2019, 7, 17, 17, 0, 0, 588000000, ModelTestHelper.ZONE))
        .endTime(ZonedDateTime.of(2019, 7, 17, 21, 0, 0, 588000000, ModelTestHelper.ZONE))
        .locationName(clarkAndMonroe().getName())
        .calendarName("threeleggedtaco")
        .build());
  }
}