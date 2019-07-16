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
import static foodtruck.schedule.ModelTestHelper.ZONE;
import static foodtruck.schedule.ModelTestHelper.clarkAndMonroe;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FatTomatoReaderTest extends AbstractReaderTest<FatTomatoReader> {

  @Mock private CalendarAddressExtractor extractor;
  @Mock private TruckDAO truckDAO;

  @Before
  public void setup() {
    this.supplier = () -> new FatTomatoReader(extractor, truckDAO, ZONE);
  }

  @Test
  public void testRead() throws IOException {
    Truck truck = Truck.builder()
        .name("Fat Tomato")
        .id("fattomatoinc")
        .build();
    when(truckDAO.findByIdOpt(truck.getId())).thenReturn(Optional.of(truck));
    when(extractor.parse(any(), any())).thenReturn(Optional.empty());
    when(extractor.parse("Crystal Lake Brewing, 150 N Main St, Crystal Lake, IL 60014, USA", truck)).thenReturn(
        Optional.of(clarkAndMonroe()));
    List<TempTruckStop> stops = execFindStop("fattomato.json");
    assertThat(stops).hasSize(2);
    assertThat(stops).contains(TempTruckStop.builder()
        .truckId("fattomatoinc")
        .startTime(ZonedDateTime.of(2019, 8, 27, 17, 0, 0, 0, ZONE))
        .endTime(ZonedDateTime.of(2019, 8, 27, 20, 0, 0, 0, ZONE))
        .locationName(clarkAndMonroe().getName())
        .calendarName("Fat Tomato")
        .build());
  }
}