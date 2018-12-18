package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
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
  @Mock private AddressExtractor extractor;

  @Before
  public void before() {
    this.reader = new CoastlineCalendarReader(extractor, truckDAO, ZONE);
  }

  @Test
  public void findStops() throws IOException {
    when(truckDAO.findByIdOpt("coastlinecove")).thenReturn(Optional.of(COASTLINE_TRUCK));
    when(extractor.parse("Wacker & Monroe ", COASTLINE_TRUCK)).thenReturn(ImmutableList.of("Wacker and Monroe, Chicago, IL"));
    when(extractor.parse("Randolph & Columbus", COASTLINE_TRUCK)).thenReturn(ImmutableList.of("Randolph and Columbus, Chicago, IL"));
    when(extractor.parse("Michigan & Monroe ", COASTLINE_TRUCK)).thenReturn(ImmutableList.of("Michigan and Monroe, Chicago, IL"));
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("coastline.html");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);
    List<TempTruckStop> stops = reader.findStops(doc);
    assertThat(stops).hasSize(4);
    assertThat(stops).contains(TempTruckStop.builder()
        .locationName("Michigan and Monroe, Chicago, IL")
        .calendarName("coastlinecove")
        .truckId("coastlinecove")
        .startTime(ZonedDateTime.of(2018,12, 22, 11, 0, 0, 0, ZONE))
        .endTime(ZonedDateTime.of(2018, 12, 22, 14, 0, 0, 0, ZONE))
        .build());
  }
}