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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.ModelTestHelper.wackerAndAdams;
import static org.mockito.Mockito.when;

/**
 * @author aviolette
 * @since 11/10/18
 */
@RunWith(MockitoJUnitRunner.class)
public class FatShallotScheduleReaderTest {

  @Mock private AddressExtractor extractor;
  @Mock private Clock clock;
  @Mock private GeoLocator geolocator;

  @Test
  public void findStops() throws IOException {
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("fatshallot.html");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);
    ZoneId zoneId = ZoneId.of("America/Chicago");
    when(clock.now8()).thenReturn(ZonedDateTime.of(2018, 11, 6, 11, 1,2, 3, zoneId));
    FatShallotScheduleReader parser = new FatShallotScheduleReader(clock, extractor, geolocator,
        zoneId);
    Truck thefatshallot = Truck.builder()
        .id("thefatshallot")
        .name("The Fat Shallot")
        .build();
    when(extractor.parse("Wacker & Adams 11-2pm", thefatshallot)).thenReturn(ImmutableList.of("Wacker and Adams, Chicago, IL"));
    when(extractor.parse("Wacker & Adams 11-3pm", thefatshallot)).thenReturn(ImmutableList.of("Wacker and Adams, Chicago, IL"));
    when(geolocator.locateOpt("Wacker and Adams, Chicago, IL")).thenReturn(Optional.of(wackerAndAdams()));
    List<TempTruckStop> items = parser.findStops(doc);
    assertThat(items).contains(TempTruckStop.builder()
        .startTime(ZonedDateTime.of(2018, 11, 6, 11, 0, 0, 0, zoneId))
        .endTime(ZonedDateTime.of(2018, 11, 6, 15, 0, 0, 0, zoneId))
        .locationName(wackerAndAdams().getName())
        .truckId("thefatshallot")
        .calendarName(parser.getCalendar())
        .build());
/*
    TruckStop.Builder builder = TruckStop.builder()
        .truck(thefatshallot);

    assertThat(items).containsExactly(builder.startTime8(ZonedDateTime.of(2018, 11, 6, 11, 0, 0, 0, zoneId))
        .endTime8(ZonedDateTime.of(2018, 11, 6, 15, 0, 0, 0, zoneId))
        .location(wackerAndAdams())
        .build(), builder.startTime8(ZonedDateTime.of(2018, 11, 7, 11, 0, 0, 0, zoneId))
        .endTime8(ZonedDateTime.of(2018, 11, 7, 14, 0, 0, 0, zoneId))
        .location(wackerAndAdams())
        .build());
        */
  }
}