package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.google.common.io.ByteStreams;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.TruckDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.ModelTestHelper.aon;
import static foodtruck.schedule.ModelTestHelper.clarkAndMonroe;
import static foodtruck.schedule.ModelTestHelper.wackerAndAdams;

/**
 * @author aviolette
 * @since 2018-12-18
 */
@RunWith(MockitoJUnitRunner.class)
public class CoastlineCalendarReaderTest extends Mockito {

  private static final Truck COASTLINE_TRUCK = Truck.builder().id("coastlinecove").name("Coastline").build();
  public static final ZoneId ZONE = ZoneId.of("America/Chicago");
  private CoastlineCalendarReader reader;

  @Mock private TruckDAO truckDAO;
  @Mock private CalendarAddressExtractor extractor;

  @Before
  public void before() {
    this.reader = new CoastlineCalendarReader(extractor, truckDAO, ZONE);
  }

  @Test
  public void findStops() throws IOException {
    when(truckDAO.findByIdOpt("coastlinecove")).thenReturn(Optional.of(COASTLINE_TRUCK));
    when(extractor.parse(anyString(), any())).thenReturn(Optional.empty());
    when(extractor.parse("AON Center", COASTLINE_TRUCK)).thenReturn(Optional.of(aon()));
    when(extractor.parse("Wacker & Monroe", COASTLINE_TRUCK)).thenReturn(Optional.of(wackerAndAdams()));
    when(extractor.parse("Clark and Monroe", COASTLINE_TRUCK)).thenReturn(Optional.of(clarkAndMonroe()));
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("coastline5.html");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);
    List<TempTruckStop> stops = reader.findStops(doc);
    assertThat(stops).hasSize(3);
    assertThat(stops).contains(TempTruckStop.builder()
        .locationName("AON")
        .calendarName("coastlinecove")
        .truckId("coastlinecove")
        .startTime(ZonedDateTime.of(2019,6, 11, 11, 0, 0, 0, ZONE))
        .endTime(ZonedDateTime.of(2019, 6, 11, 14, 0, 0, 0, ZONE))
        .build());
  }
}